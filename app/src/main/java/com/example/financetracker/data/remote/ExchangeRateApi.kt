package com.example.financetracker.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Volání serveru s kurzy (cesty viz [ApiRoutes]). Instance: `ApiClient.exchangeRateApi`.
 *
 * Kurz EUR → CZK:
 * ```
 * val response = ApiClient.exchangeRateApi.latest(base = "EUR", symbols = "CZK")
 * val rate = response.rates.getValue("CZK")   // 24.369
 * ```
 * Funkce jsou suspend → volat z coroutiny (viewModelScope.launch { … }).
 * Bez internetu vyhodí IOException, chybová odpověď serveru HttpException.
 */
interface ExchangeRateApi {

    @GET(ApiRoutes.LATEST)
    suspend fun latest(
        @Query(ApiRoutes.QUERY_BASE) base: String,
        @Query(ApiRoutes.QUERY_SYMBOLS) symbols: String = "CZK",
        @Query(ApiRoutes.QUERY_AMOUNT) amount: Double? = null,
    ): RatesResponse

    /** [date] ve formátu YYYY-MM-DD, např. LocalDate.toString(). */
    @GET(ApiRoutes.HISTORICAL)
    suspend fun historical(
        @Path(ApiRoutes.PATH_DATE) date: String,
        @Query(ApiRoutes.QUERY_BASE) base: String,
        @Query(ApiRoutes.QUERY_SYMBOLS) symbols: String = "CZK",
    ): RatesResponse

    @GET(ApiRoutes.TIME_SERIES)
    suspend fun timeSeries(
        @Path(ApiRoutes.PATH_START) start: String,
        @Path(ApiRoutes.PATH_END) end: String,
        @Query(ApiRoutes.QUERY_BASE) base: String,
        @Query(ApiRoutes.QUERY_SYMBOLS) symbols: String = "CZK",
    ): TimeSeriesResponse

    /** Kód měny → název, např. "EUR" → "Euro". */
    @GET(ApiRoutes.CURRENCIES)
    suspend fun currencies(): Map<String, String>
}

/** Odpověď [ExchangeRateApi.latest] a [ExchangeRateApi.historical]. */
data class RatesResponse(
    val amount: Double,
    val base: String,
    /** Den, ke kterému kurz platí (YYYY-MM-DD). */
    val date: String,
    /** Cílová měna → kolik jí stojí [amount] jednotek [base], např. "CZK" → 24.369. */
    val rates: Map<String, Double>,
)

/** Odpověď [ExchangeRateApi.timeSeries]. */
data class TimeSeriesResponse(
    val amount: Double,
    val base: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    /** Datum (YYYY-MM-DD) → (měna → kurz). Víkendy a svátky chybí. */
    val rates: Map<String, Map<String, Double>>,
)
