package com.example.financetracker.data.repository

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** Paměť posledního staženého kurzu každé měny – použije se, když není internet. */
interface ExchangeRateCache {
    suspend fun load(currency: String): ExchangeRate?
    suspend fun save(rate: ExchangeRate)
}

/** Kurzy uložené v SharedPreferences, takže vydrží i restart aplikace. */
class SharedPreferencesRateCache(context: Context) : ExchangeRateCache {

    private val prefs = context.getSharedPreferences("exchange_rates", Context.MODE_PRIVATE)

    override suspend fun load(currency: String): ExchangeRate? = withContext(Dispatchers.IO) {
        val rate = prefs.getString(rateKey(currency), null)?.toDoubleOrNull()
        val date = prefs.getString(dateKey(currency), null)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        if (rate == null || date == null) return@withContext null
        ExchangeRate(
            currency = currency,
            rateToCzk = rate,
            date = date,
            source = prefs.getString(sourceKey(currency), null).orEmpty(),
            isCached = true,
        )
    }

    override suspend fun save(rate: ExchangeRate) = withContext(Dispatchers.IO) {
        prefs.edit {
            putString(rateKey(rate.currency), rate.rateToCzk.toString())
            putString(dateKey(rate.currency), rate.date.toString())
            putString(sourceKey(rate.currency), rate.source)
        }
    }

    private fun rateKey(currency: String) = "$currency.rate"
    private fun dateKey(currency: String) = "$currency.date"
    private fun sourceKey(currency: String) = "$currency.source"
}
