package com.example.financetracker.ui.util

import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

/*
 * Formátování pro celou aplikaci (pravidlo z HANDOFF.md: české formáty, částky vždy s .tabular()).
 */

val CzechLocale: Locale = Locale.forLanguageTag("cs-CZ")

/** 1234.5 → "1 234,50 Kč" */
fun formatCzk(amount: Double): String = formatAmount(amount, "CZK")

/** 12.5, "EUR" → "12,50 €" */
fun formatAmount(amount: Double, currency: String): String =
    NumberFormat.getCurrencyInstance(CzechLocale).apply {
        runCatching { this.currency = Currency.getInstance(currency) }
    }.format(amount)

private val dateFormatter = DateTimeFormatter.ofPattern("d. M. yyyy", CzechLocale)

/** 2026-10-26 → "26. 10. 2026" */
fun formatDate(date: LocalDate): String = date.format(dateFormatter)

private val monthFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", CzechLocale)

/** 2026-10 → "Říjen 2026" */
fun formatMonth(month: YearMonth): String =
    month.format(monthFormatter).replaceFirstChar { it.titlecase(CzechLocale) }

private val weekdayFormatter = DateTimeFormatter.ofPattern("EEEE d. M.", CzechLocale)
private val weekdayYearFormatter = DateTimeFormatter.ofPattern("EEEE d. M. yyyy", CzechLocale)

/** "Dnes", "Včera", "Pondělí 9. 11.", u jiného roku i s rokem. */
fun formatDay(date: LocalDate, today: LocalDate = LocalDate.now()): String = when (date) {
    today -> "Dnes"
    today.minusDays(1) -> "Včera"
    else -> date.format(if (date.year == today.year) weekdayFormatter else weekdayYearFormatter)
        .replaceFirstChar { it.titlecase(CzechLocale) }
}

/** Rozsah týdne: "9.–15. 11. 2026", přes dva měsíce "27. 10. – 2. 11. 2026", přes rok s oběma roky. */
fun formatWeek(from: LocalDate, to: LocalDate): String = when {
    from.year != to.year -> "${formatDate(from)} – ${formatDate(to)}"
    from.month != to.month -> "${from.dayOfMonth}. ${from.monthValue}. – ${formatDate(to)}"
    else -> "${from.dayOfMonth}.–${formatDate(to)}"
}

/** "1 234,50" / "1234.5" → 1234.5, neplatný vstup → null. */
fun parseAmount(text: String): Double? =
    text.replace(" ", "").replace(" ", "").replace(',', '.').toDoubleOrNull()
