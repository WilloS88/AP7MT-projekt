package com.example.financetracker.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CurrencyConversionTest {

    @Test
    fun convertsByRate() {
        assertEquals(243.69, convertToCzk(10.0, 24.369), 0.0)
    }

    @Test
    fun roundsToHalerHalfUp() {
        assertEquals(1.01, convertToCzk(1.005, 1.0), 0.0)
        assertEquals(6.63, convertToCzk(100.0, 0.066319), 0.0)
    }

    @Test
    fun czkStaysTheSame() {
        assertEquals(1234.5, convertToCzk(1234.5, 1.0), 0.0)
    }

    @Test
    fun zeroAmountIsZero() {
        assertEquals(0.0, convertToCzk(0.0, 24.369), 0.0)
    }

    @Test
    fun usesRateFromExchangeRate() {
        val rate = ExchangeRate("EUR", 25.0, LocalDate.of(2026, 10, 26), "test", isCached = false)
        assertEquals(312.5, convertToCzk(12.5, rate), 0.0)
    }
}
