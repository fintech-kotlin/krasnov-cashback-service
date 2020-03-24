package ru.tinkoff.fintech.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
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
class ProcessTransactionService(
    private val cardServiceClient: CardServiceClient,
    private val clientService: ClientService,
    private val loyaltyServiceClient: LoyaltyServiceClient,
    private val notificationService: NotificationService,
    private val cashbackCalculator: CashbackCalculator,
    private val loyaltyPaymentRepository: LoyaltyPaymentRepository,
    @Value("\${sign}") private val sign: String
) {

    fun processTransaction(transaction: Transaction) = CoroutineScope(Dispatchers.IO).launch {
        val card = cardServiceClient.getCard(transaction.cardNumber)

        val asyncClient = async { clientService.getClient(card.client) }
        val asyncLoyaltyProgram = async { loyaltyServiceClient.getLoyaltyProgram(card.loyaltyProgram) }

        val cashbackTotalValueAsync = async {
            loyaltyPaymentRepository.findAllBySignAndCardIdAndDateTimeAfter(
                sign,
                card.id,
                LocalDate.now().minusMonths(1).atStartOfDay()
            ).map { loyaltyPayment -> loyaltyPayment.value }.sum()
        }

        val client = asyncClient.await()
        val loyaltyProgram = asyncLoyaltyProgram.await()

        val amount =
            cashbackCalculator.calculateCashback(
                TransactionInfo(
                    loyaltyProgramName = loyaltyProgram.name,
                    transactionSum = transaction.value,
                    cashbackTotalValue = cashbackTotalValueAsync.await(),
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

        loyaltyPaymentRepository.save(
            LoyaltyPaymentEntity(
                sign = sign,
                value = amount,
                cardId = card.id,
                dateTime = LocalDateTime.now(),
                transactionId = transaction.transactionId
            )
        )
    }
}