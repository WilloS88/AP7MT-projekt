package com.example.financetracker.feature.stats

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PeriodTest {

    // Úterý
    private val today = LocalDate.of(2026, 11, 10)

    @Test
    fun dayIsJustToday() {
        val period = Period(PeriodType.Day, 0, today)

        assertEquals(today, period.from)
        assertEquals(today, period.to)
        assertEquals("Dnes", period.label)
    }

    @Test
    fun previousDaysHaveNames() {
        assertEquals("Včera", Period(PeriodType.Day, -1, today).label)
        assertEquals("Neděle 8. 11.", Period(PeriodType.Day, -2, today).label)
    }

    @Test
    fun dayInPreviousYearShowsYear() {
        val period = Period(PeriodType.Day, -365, today)

        assertEquals("Pondělí 10. 11. 2025", period.label)
    }

    @Test
    fun weekStartsOnMonday() {
        val period = Period(PeriodType.Week, 0, today)

        assertEquals(LocalDate.of(2026, 11, 9), period.from)
        assertEquals(LocalDate.of(2026, 11, 15), period.to)
        assertEquals("9.–15. 11. 2026", period.label)
    }

    @Test
    fun weekOnSundayStillStartsOnMonday() {
        val period = Period(PeriodType.Week, 0, LocalDate.of(2026, 11, 15))

        assertEquals(LocalDate.of(2026, 11, 9), period.from)
    }

    @Test
    fun weekAcrossMonths() {
        val period = Period(PeriodType.Week, -2, today)

        assertEquals(LocalDate.of(2026, 10, 26), period.from)
        assertEquals(LocalDate.of(2026, 11, 1), period.to)
        assertEquals("26. 10. – 1. 11. 2026", period.label)
    }

    @Test
    fun weekAcrossYears() {
        val period = Period(PeriodType.Week, 0, LocalDate.of(2026, 1, 1))

        assertEquals(LocalDate.of(2025, 12, 29), period.from)
        assertEquals("29. 12. 2025 – 4. 1. 2026", period.label)
    }

    @Test
    fun monthAndYearRanges() {
        val month = Period(PeriodType.Month, -1, today)
        val year = Period(PeriodType.Year, 0, today)

        assertEquals(LocalDate.of(2026, 10, 1), month.from)
        assertEquals(LocalDate.of(2026, 10, 31), month.to)
        assertEquals(LocalDate.of(2026, 1, 1), year.from)
        assertEquals(LocalDate.of(2026, 12, 31), year.to)
        assertEquals("2026", year.label)
    }

    @Test
    fun cannotGoToFuture() {
        assertFalse(Period(PeriodType.Week, 0, today).canGoNext)
        assertTrue(Period(PeriodType.Week, -1, today).canGoNext)
    }
}
