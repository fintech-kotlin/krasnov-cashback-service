package ru.tinkoff.fintech.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.tinkoff.fintech.client.CardServiceClient
import ru.tinkoff.fintech.client.ClientService
import ru.tinkoff.fintech.client.LoyaltyServiceClient
import ru.tinkoff.fintech.db.entity.LoyaltyPaymentEntity
import ru.tinkoff.fintech.db.repository.LoyaltyPaymentRepository
import ru.tinkoff.fintech.model.NotificationMessageInfo
import ru.tinkoff.fintech.model.Transaction
import ru.tinkoff.fintech.model.TransactionInfo
import ru.tinkoff.fintech.service.cashback.CashbackCalculator
import ru.tinkoff.fintech.service.notification.NotificationService
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class ProcessTransactionService @Autowired constructor(
    private val cardServiceClient: CardServiceClient,
    private val clientService: ClientService,
    private val loyaltyServiceClient: LoyaltyServiceClient,
    private val notificationService: NotificationService,
    private val cashbackCalculator: CashbackCalculator,
    private val loyaltyPaymentRepository: LoyaltyPaymentRepository
) {

    @Value("\${sign}")
    private val sign: String = ""

    fun processTransaction(transaction: Transaction) {
        val card = cardServiceClient.getCard(transaction.cardNumber)

        val client = clientService.getClient(card.client)

        val loyaltyProgram = loyaltyServiceClient.getLoyaltyProgram(card.loyaltyProgram)

        val amount =
            cashbackCalculator.calculateCashback(
                TransactionInfo(
                    loyaltyProgramName = loyaltyProgram.name,
                    transactionSum = transaction.value,
                    cashbackTotalValue = this.calculateTotalAmount(card.id),
                    mccCode = transaction.mccCode,
                    clientBirthDate = client.birthDate,
                    firstName = client.firstName,
                    middleName = client.middleName,
                    lastName = client.lastName
                )
            )

        notificationService.sendNotification(
            client.id,
            NotificationMessageInfo(
                cashback = amount,
                cardNumber = transaction.cardNumber,
                name = client.firstName,
                transactionSum = transaction.value,
                transactionDate = transaction.time,
                category = loyaltyProgram.name
            )
        )

        this.saveLoyaltyPayment(card.id, transaction.transactionId, amount)
    }

    private fun calculateTotalAmount(cardId: String): Double {
        return loyaltyPaymentRepository.findAllBySignAndCardIdAndDateTimeAfter(
            sign,
            cardId,
            LocalDate.now().minusMonths(1).atStartOfDay()
        ).map { loyaltyPayment -> loyaltyPayment.value }.sum()
    }

    private fun saveLoyaltyPayment(cardId: String, transactionId: String, cashbackAmount: Double) {
        val loyaltyPaymentEntity = LoyaltyPaymentEntity(
            sign = sign,
            value = cashbackAmount,
            cardId = cardId,
            dateTime = LocalDateTime.now(),
            transactionId = transactionId
        )
        loyaltyPaymentRepository.save(loyaltyPaymentEntity)
    }
}