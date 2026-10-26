package com.example.financetracker.data.repository

import java.time.LocalDate

/** Kurz jedné měny vůči Kč. */
data class ExchangeRate(
    val currency: String,
    /** Kolik Kč stojí 1 jednotka [currency]. */
    val rateToCzk: Double,
    val date: LocalDate,
    /** Např. "Frankfurter (ECB)". */
    val source: String,
    /** true = kurz z cache, protože nebylo připojení. */
    val isCached: Boolean,
)

/**
 * Kontrakt pro kurzy měn (implementace přes REST API).
 * Formulář výdaje i sken používají jen toto rozhraní.
 */
interface ExchangeRateRepository {
    /** Měny, které nabízí výběr měny (CZK první). */
    val supportedCurrencies: List<String>

    /** Aktuální kurz [currency] → CZK. Pro "CZK" vrací 1.0. Při chybě sítě vrátí cache nebo vyhodí výjimku. */
    suspend fun getRateToCzk(currency: String): ExchangeRate
}

/**
 * Dočasná implementace s pevnými kurzy, aby šel formulář zkoušet hned.
 * V AppContainer se pak nahradí skutečným API.
 */
class FakeExchangeRateRepository : ExchangeRateRepository {
    private val rates = mapOf("CZK" to 1.0, "EUR" to 25.0, "USD" to 23.0, "GBP" to 29.5, "PLN" to 5.8)

    override val supportedCurrencies: List<String> = rates.keys.toList()

    override suspend fun getRateToCzk(currency: String): ExchangeRate = ExchangeRate(
        currency = currency,
        rateToCzk = rates[currency] ?: error("Neznámá měna $currency"),
        date = LocalDate.now(),
        source = "Fake (pevné kurzy)",
        isCached = false,
    )
}
