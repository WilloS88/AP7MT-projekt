package com.example.financetracker.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class AmountInputTest {

    @Test
    fun keepsDigitsAndComma() {
        assertEquals("1234,5", sanitizeAmountInput("1234,5"))
    }

    @Test
    fun dotBecomesComma() {
        assertEquals("12,5", sanitizeAmountInput("12.5"))
    }

    @Test
    fun onlyFirstSeparatorCounts() {
        assertEquals("1,23", sanitizeAmountInput("1,2,3"))
    }

    @Test
    fun maxTwoDecimals() {
        assertEquals("9,99", sanitizeAmountInput("9,999"))
    }

    @Test
    fun dropsLettersAndSpaces() {
        assertEquals("1234,50", sanitizeAmountInput("1 234,50 Kč"))
    }

    @Test
    fun leadingZeros() {
        assertEquals("7", sanitizeAmountInput("007"))
        assertEquals("0", sanitizeAmountInput("0"))
        assertEquals("0,5", sanitizeAmountInput(",5"))
        assertEquals("", sanitizeAmountInput(""))
    }

    @Test
    fun limitsIntegerDigits() {
        assertEquals("123456789", sanitizeAmountInput("1234567890"))
    }
}
