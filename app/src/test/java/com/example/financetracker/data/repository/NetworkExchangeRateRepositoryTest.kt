package com.example.financetracker.data.repository

import com.example.financetracker.data.remote.ExchangeRateApi
import com.example.financetracker.data.remote.RatesResponse
import com.example.financetracker.data.remote.TimeSeriesResponse
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

class NetworkExchangeRateRepositoryTest {

    private val api = FakeApi()
    private val cache = InMemoryCache()
    private val repository = NetworkExchangeRateRepository(api, cache)

    @Test
    fun onlineReturnsFreshRateAndRemembersIt() = runBlocking {
        val rate = repository.getRateToCzk("EUR")

        assertEquals(24.369, rate.rateToCzk, 0.0)
        assertEquals(LocalDate.of(2026, 10, 26), rate.date)
        assertFalse(rate.isCached)
        assertEquals(rate, cache.rates["EUR"])
    }

    @Test
    fun offlineReturnsLastRateMarkedAsCached() = runBlocking {
        repository.getRateToCzk("EUR")
        api.online = false

        val rate = repository.getRateToCzk("EUR")

        assertEquals(24.369, rate.rateToCzk, 0.0)
        assertTrue(rate.isCached)
    }

    @Test(expected = IOException::class)
    fun offlineWithoutCacheThrows() {
        api.online = false
        runBlocking { repository.getRateToCzk("EUR") }
    }

    @Test
    fun czkIsAlwaysOneWithoutNetwork() = runBlocking {
        api.online = false

        val rate = repository.getRateToCzk("CZK")

        assertEquals(1.0, rate.rateToCzk, 0.0)
        assertEquals(0, api.calls)
    }

    @Test
    fun supportedCurrenciesStartWithCzk() {
        assertEquals("CZK", repository.supportedCurrencies.first())
    }

    private class FakeApi : ExchangeRateApi {
        var online = true
        var calls = 0

        override suspend fun latest(base: String, symbols: String, amount: Double?): RatesResponse {
            calls++
            if (!online) throw IOException("offline")
            return RatesResponse(amount = 1.0, base = base, date = "2026-10-26", rates = mapOf("CZK" to 24.369))
        }

        override suspend fun historical(date: String, base: String, symbols: String): RatesResponse =
            throw UnsupportedOperationException()

        override suspend fun timeSeries(start: String, end: String, base: String, symbols: String): TimeSeriesResponse =
            throw UnsupportedOperationException()

        override suspend fun currencies(): Map<String, String> = throw UnsupportedOperationException()
    }

    private class InMemoryCache : ExchangeRateCache {
        val rates = mutableMapOf<String, ExchangeRate>()
        override suspend fun load(currency: String): ExchangeRate? = rates[currency]
        override suspend fun save(rate: ExchangeRate) {
            rates[rate.currency] = rate
        }
    }
}
