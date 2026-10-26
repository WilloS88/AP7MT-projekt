package com.example.financetracker.feature.importer

import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.time.LocalDate

/** Jeden výdaj nalezený ve výpisu (příjmy se do importu nedostanou). */
data class StatementTransaction(
    val date: LocalDate,
    /** Utracená částka v [currency], vždy kladná. */
    val amount: Double,
    /** ISO kód měny transakce. */
    val currency: String,
    /** Částka v Kč, pokud ji výpis uvádí (účet v Kč); jinak null a dopočítá se kurzem. */
    val amountCzk: Double?,
    val note: String,
    /** Původní řádek výpisu, pro kontrolu v náhledu. */
    val rawLine: String,
)

/** Soubor nejde načíst; [message] se ukáže uživateli. */
class StatementException(val title: String, override val message: String) : Exception(message)

/**
 * Částka z výpisu: "-1 234,56", "-1234.56", "−5 000.00", "1,234.56 CZK" → číslo, jinak null.
 * Desetinný oddělovač je poslední čárka nebo tečka, pokud za ní jsou nejvýš 2 číslice.
 */
fun parseStatementAmount(text: String): Double? {
    var value = text
        .replace('−', '-') // typografické mínus
        .filter { it.isDigit() || it == '-' || it == '+' || it == ',' || it == '.' }
    if (value.isEmpty()) return null
    val negative = value.startsWith('-') || value.endsWith('-')
    value = value.trim('-', '+')

    val separator = value.lastIndexOfAny(charArrayOf(',', '.'))
    val normalized = if (separator >= 0 && value.length - separator - 1 in 1..2) {
        value.substring(0, separator).filter(Char::isDigit) + "." + value.substring(separator + 1)
    } else {
        value.filter(Char::isDigit)
    }
    val number = normalized.toDoubleOrNull() ?: return null
    return if (negative) -number else number
}

private val dottedDate = Regex("""(\d{1,2})\.\s?(\d{1,2})\.\s?(\d{4})""")
private val isoDate = Regex("""(\d{4})-(\d{2})-(\d{2})""")
private val slashDate = Regex("""(\d{1,2})/(\d{1,2})/(\d{4})""")

/** Datum z výpisu: "01.09.2026", "1. 9. 2026", "2026-09-01", "01/09/2026" (i s časem za ním). */
fun parseStatementDate(text: String): LocalDate? {
    dottedDate.find(text)?.let { m ->
        val (d, mo, y) = m.destructured
        return runCatching { LocalDate.of(y.toInt(), mo.toInt(), d.toInt()) }.getOrNull()
    }
    isoDate.find(text)?.let { m ->
        val (y, mo, d) = m.destructured
        return runCatching { LocalDate.of(y.toInt(), mo.toInt(), d.toInt()) }.getOrNull()
    }
    slashDate.find(text)?.let { m ->
        val (d, mo, y) = m.destructured
        return runCatching { LocalDate.of(y.toInt(), mo.toInt(), d.toInt()) }.getOrNull()
    }
    return null
}

/**
 * Text CSV souboru: UTF-8 (i s BOM), UTF-16 s BOM, jinak Windows-1250 (starší exporty bank).
 */
fun decodeStatementText(bytes: ByteArray): String {
    val bom = bytes.take(3).map { it.toInt() and 0xFF }
    return when {
        bom == listOf(0xEF, 0xBB, 0xBF) -> String(bytes, 3, bytes.size - 3, Charsets.UTF_8)
        bom.take(2) == listOf(0xFF, 0xFE) -> String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE)
        bom.take(2) == listOf(0xFE, 0xFF) -> String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE)
        else -> try {
            Charsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString()
        } catch (e: CharacterCodingException) {
            String(bytes, Charset.forName("windows-1250"))
        }
    }
}
