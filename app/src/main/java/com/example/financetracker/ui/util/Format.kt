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

/** "1 234,50" / "1234.5" → 1234.5, neplatný vstup → null. */
fun parseAmount(text: String): Double? =
    text.replace(" ", "").replace(" ", "").replace(',', '.').toDoubleOrNull()
