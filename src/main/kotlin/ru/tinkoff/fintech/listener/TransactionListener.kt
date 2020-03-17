package ru.tinkoff.fintech.listener

import mu.KLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import ru.tinkoff.fintech.model.Transaction
import ru.tinkoff.fintech.service.ProcessTransactionService

@Component
class TransactionListener (private val processTransactionService: ProcessTransactionService) {
    companion object : KLogging()

    @KafkaListener(
        topics = ["\${kafka.consumer.topic}"],
        groupId = "\${kafka.consumer.groupId}"
    )
    fun onMessage(transaction: Transaction) {
        try {
            logger.info("Transaction: $transaction")
            processTransactionService.processTransaction(transaction)
        } catch (e: Exception) {
            logger.error("Transaction processing failed", e)
        }
    }
}