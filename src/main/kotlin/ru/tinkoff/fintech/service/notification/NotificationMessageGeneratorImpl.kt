package ru.tinkoff.fintech.service.notification

import ru.tinkoff.fintech.model.NotificationMessageInfo

class NotificationMessageGeneratorImpl(
    private val cardNumberMasker: CardNumberMasker
) : NotificationMessageGenerator {

    override fun generateMessage(notificationMessageInfo: NotificationMessageInfo): String {
        return "Уважаемый, ${notificationMessageInfo.name}!" +
                "\nСпешим Вам сообщить, что на карту ${cardNumberMasker.mask(notificationMessageInfo.cardNumber)}" +
                "\nначислен cashback в размере ${notificationMessageInfo.cashback}" +
                "\nза категорию ${notificationMessageInfo.category}." +
                "\nСпасибо за покупку ${notificationMessageInfo.transactionDate}"
    }
}