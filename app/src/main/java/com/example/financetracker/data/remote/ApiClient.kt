package com.example.financetracker.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Jedna sdílená instance Retrofitu pro celou aplikaci. */
object ApiClient {

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiRoutes.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val exchangeRateApi: ExchangeRateApi by lazy { retrofit.create(ExchangeRateApi::class.java) }
}
