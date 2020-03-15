package ru.tinkoff.fintech.service.notification

import org.springframework.stereotype.Service

@Service
class CardNumberMaskerImpl : CardNumberMasker {

    override fun mask(cardNumber: String, maskChar: Char, start: Int, end: Int): String = when {
        start > end -> throw Exception("Start index cannot be greater than end index")
        end > cardNumber.length -> cardNumber.substring(0, start) +
                ((maskChar.toString())).repeat(cardNumber.length - start)
        else -> cardNumber.substring(0, start) +
                ((maskChar.toString())).repeat(end - start) +
                cardNumber.substring(end, cardNumber.length)

    }
}