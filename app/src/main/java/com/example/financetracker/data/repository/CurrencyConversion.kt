package com.example.financetracker.data.repository

import java.math.BigDecimal
import java.math.RoundingMode

/** Základní měna aplikace – všechny přehledy počítají v ní. */
const val BASE_CURRENCY = "CZK"

/**
 * Přepočet částky na Kč, zaokrouhleno na haléře (matematicky, 0,005 → 0,01).
 * [rateToCzk] = kolik Kč stojí 1 jednotka cizí měny (viz [ExchangeRate.rateToCzk]).
 */
fun convertToCzk(amount: Double, rateToCzk: Double): Double =
    BigDecimal.valueOf(amount)
        .multiply(BigDecimal.valueOf(rateToCzk))
        .setScale(2, RoundingMode.HALF_UP)
        .toDouble()

/** Přepočet částky na Kč podle načteného kurzu. */
fun convertToCzk(amount: Double, rate: ExchangeRate): Double = convertToCzk(amount, rate.rateToCzk)
