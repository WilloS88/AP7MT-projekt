package com.example.financetracker.data.repository

import com.example.financetracker.data.remote.ExchangeRateApi
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException

/**
 * Kurzy z Frankfurter API (kurzy ECB, viz ApiRoutes).
 *
 * Každý stažený kurz se uloží do [cache]. Bez internetu (nebo při chybě serveru) se vrátí
 * poslední uložený kurz s `isCached = true`; když žádný uložený není, výjimka se pošle dál.
 */
class NetworkExchangeRateRepository(
    private val api: ExchangeRateApi,
    private val cache: ExchangeRateCache,
) : ExchangeRateRepository {

    /** Nejčastější měny nahoře, všechny jsou v kurzech ECB. */
    override val supportedCurrencies: List<String> = listOf(
        BASE_CURRENCY, "EUR", "USD", "GBP", "PLN", "HUF", "CHF",
        "DKK", "SEK", "NOK", "RON", "TRY", "JPY", "CNY", "CAD", "AUD",
    )

    override suspend fun getRateToCzk(currency: String): ExchangeRate {
        if (currency == BASE_CURRENCY) {
            return ExchangeRate(BASE_CURRENCY, 1.0, LocalDate.now(), SOURCE, isCached = false)
        }
        return try {
            val response = api.latest(base = currency, symbols = BASE_CURRENCY)
            val rate = ExchangeRate(
                currency = currency,
                rateToCzk = response.rates.getValue(BASE_CURRENCY) / response.amount,
                date = LocalDate.parse(response.date),
                source = SOURCE,
                isCached = false,
            )
            cache.save(rate)
            rate
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            cache.load(currency)?.copy(isCached = true) ?: throw e
        }
    }

    companion object {
        const val SOURCE = "Frankfurter (ECB)"
    }
}
