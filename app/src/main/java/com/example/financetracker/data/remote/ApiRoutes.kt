package com.example.financetracker.data.remote

/**
 * Všechny endpointy serveru na jednom místě.
 *
 * Server: Frankfurter API (kurzy ECB, JSON, zdarma, bez API klíče) – https://frankfurter.dev
 * Kurzy se aktualizují každý pracovní den kolem 16:00 SEČ, o víkendu vrací poslední pátek.
 *
 * Retrofit skládá URL jako BASE_URL + cesta, např. BASE_URL + LATEST → https://api.frankfurter.dev/v1/latest
 */
object ApiRoutes {

    const val BASE_URL = "https://api.frankfurter.dev/v1/"

    // ---- Názvy parametrů

    /** Cesta: datum YYYY-MM-DD. */
    const val PATH_DATE = "date"
    const val PATH_START = "start"
    const val PATH_END = "end"

    /** Query: výchozí měna (z jaké měny se převádí), např. "EUR". */
    const val QUERY_BASE = "base"

    /** Query: cílové měny oddělené čárkou, např. "CZK" nebo "CZK,USD". */
    const val QUERY_SYMBOLS = "symbols"

    /** Query: částka k převodu (výchozí 1). */
    const val QUERY_AMOUNT = "amount"

    // ---- Endpointy

    /**
     * Nejnovější kurz.
     * GET latest?base=EUR&symbols=CZK
     * → {"amount":1.0,"base":"EUR","date":"2026-10-26","rates":{"CZK":24.369}}
     */
    const val LATEST = "latest"

    /**
     * Kurz k danému dni (YYYY-MM-DD). Pro víkend/svátek vrátí poslední pracovní den.
     * GET 2026-10-20?base=USD&symbols=CZK
     * → {"amount":1.0,"base":"USD","date":"2026-10-20","rates":{"CZK":21.693}}
     */
    const val HISTORICAL = "{$PATH_DATE}"

    /**
     * Kurzy za období (např. pro graf vývoje kurzu).
     * GET 2026-10-01..2026-10-05?base=EUR&symbols=CZK
     * → {"amount":1.0,"base":"EUR","start_date":"2026-10-01","end_date":"2026-10-05",
     *    "rates":{"2026-10-01":{"CZK":24.459},"2026-10-02":{"CZK":24.47}}}
     */
    const val TIME_SERIES = "{$PATH_START}..{$PATH_END}"

    /**
     * Seznam podporovaných měn (kód → anglický název).
     * GET currencies
     * → {"AUD":"Australian Dollar","CZK":"Czech Koruna","EUR":"Euro",…}
     */
    const val CURRENCIES = "currencies"
}
