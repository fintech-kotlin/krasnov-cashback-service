package ru.tinkoff.fintech.service.notification

class CardNumberMaskerImpl : CardNumberMasker {

    override fun mask(cardNumber: String, maskChar: Char, start: Int, end: Int): String = when {
        start < 0 || end < 0 -> ""
        start > cardNumber.length -> throw Exception("Start index cannot be greater than end index")
        end > cardNumber.length -> cardNumber.substring(0, start) +
                ((maskChar.toString())).repeat(cardNumber.length - start)
        else -> cardNumber.substring(0, start) +
                ((maskChar.toString())).repeat(end - start) +
                cardNumber.substring(end, cardNumber.length)

    }
}