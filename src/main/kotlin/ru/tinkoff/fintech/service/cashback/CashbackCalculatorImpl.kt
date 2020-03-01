package ru.tinkoff.fintech.service.cashback

import org.apache.commons.lang3.StringUtils
import org.apache.commons.math3.util.ArithmeticUtils
import org.apache.commons.text.similarity.LevenshteinDistance
import ru.tinkoff.fintech.model.TransactionInfo
import java.util.*
import kotlin.math.round

internal const val LOYALTY_PROGRAM_BLACK = "BLACK"
internal const val LOYALTY_PROGRAM_ALL = "ALL"
internal const val LOYALTY_PROGRAM_BEER = "BEER"
internal const val MAX_CASH_BACK = 3000.0
internal const val MCC_SOFTWARE = 5734
internal const val MCC_BEER = 5921

class CashbackCalculatorImpl : CashbackCalculator {

    override fun calculateCashback(transactionInfo: TransactionInfo): Double {
        var cashBackSum = 0.0

        if (transactionInfo.cashbackTotalValue >= MAX_CASH_BACK)
            return cashBackSum

        if (transactionInfo.transactionSum % 666 == 0.0) {
            cashBackSum += 6.66
        }

        when {
            LOYALTY_PROGRAM_BLACK == transactionInfo.loyaltyProgramName ->
                cashBackSum = calcBlackProgramCashBack(transactionInfo, cashBackSum)
            LOYALTY_PROGRAM_ALL == transactionInfo.loyaltyProgramName
                    && MCC_SOFTWARE == transactionInfo.mccCode
                    && isPalindrome(transactionInfo.transactionSum) ->
                cashBackSum = calcAllProgramWithSoftwareMccAndPalindromeCheckCashBack(transactionInfo, cashBackSum)
            LOYALTY_PROGRAM_BEER == transactionInfo.loyaltyProgramName && MCC_BEER == transactionInfo.mccCode ->
                cashBackSum = calcBeerProgramWithBeerMccCashBack(transactionInfo, cashBackSum)

        }

        if (transactionInfo.cashbackTotalValue + cashBackSum > MAX_CASH_BACK)
            cashBackSum = MAX_CASH_BACK - transactionInfo.cashbackTotalValue

        return round(cashBackSum * 100) / 100
    }

    fun calcBlackProgramCashBack(transactionInfo: TransactionInfo, cashBackSum: Double): Double {
        return cashBackSum + transactionInfo.transactionSum * 0.01
    }

    fun calcAllProgramWithSoftwareMccAndPalindromeCheckCashBack(
        transactionInfo: TransactionInfo,
        cashBackSum: Double
    ): Double {
        return cashBackSum + transactionInfo.transactionSum *
                ArithmeticUtils.lcm(transactionInfo.firstName.length, transactionInfo.lastName.length) / 1000.0 / 100
    }

    fun calcBeerProgramWithBeerMccCashBack(transactionInfo: TransactionInfo, cashBackSum: Double): Double {
        var cashBack = 0.0
        when {
            transactionInfo.firstName.equals("Олег", true)
                    && transactionInfo.lastName.equals("Олегов", true) ->
                cashBack = transactionInfo.transactionSum * 0.1
            transactionInfo.firstName.equals("Олег", true) ->
                cashBack = transactionInfo.transactionSum * 0.07
            getMonthNameByCurrentWithAmendment()[0].equals(transactionInfo.firstName[0], true) ->
                cashBack = transactionInfo.transactionSum * 0.05
            getMonthNameByCurrentWithAmendment(-1)[0].equals(transactionInfo.firstName[0], true)
                    || getMonthNameByCurrentWithAmendment(1)[0].equals(transactionInfo.firstName[0], true) ->
                cashBack = transactionInfo.transactionSum * 0.03
        }
        return cashBackSum + cashBack
    }


    fun isPalindrome(transactionSum: Double): Boolean {
        val numberInStringForCheck = transactionSum.toString().replace(".", "")
        val distance = LevenshteinDistance().apply(numberInStringForCheck, StringUtils.reverse(numberInStringForCheck))
        return 0 == distance || 2 == distance
    }

    fun getMonthNameByCurrentWithAmendment(amendment: Int = 0): String {
        var calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, amendment)
        return calendar.getDisplayName(
            Calendar.MONTH,
            Calendar.LONG_FORMAT, Locale("ru")
        )
    }
}