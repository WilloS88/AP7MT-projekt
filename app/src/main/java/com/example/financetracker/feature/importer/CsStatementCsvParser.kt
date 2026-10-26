package com.example.financetracker.feature.importer

import com.example.financetracker.data.repository.BASE_CURRENCY
import java.text.Normalizer

/**
 * CSV export pohybů z George (Česká spořitelna) → výdaje.
 *
 * Sloupce se hledají podle názvu v hlavičce (česky i anglicky, bez ohledu na diakritiku a pořadí),
 * takže nevadí, když si uživatel v exportu sloupce přeskládá. Oddělovač `;`, `,` nebo tabulátor,
 * hodnoty mohou být v uvozovkách. Bere se jen záporná částka (výdaj).
 */
object CsStatementCsvParser {

    private val dateColumns = listOf(
        "datum provedeni", "datum transakce", "datum zauctovani", "datum",
        "processing date", "transaction date", "booking date", "date",
    )
    private val amountColumns = listOf("castka", "castka v mene uctu", "zauctovana castka", "objem", "amount")
    private val currencyColumns = listOf("mena", "mena uctu", "currency")
    private val originalAmountColumns = listOf("puvodni castka", "castka v puvodni mene", "castka v cizi mene", "original amount")
    private val originalCurrencyColumns = listOf("puvodni mena", "cizi mena", "original currency")
    private val noteColumns = listOf(
        "zprava pro prijemce", "poznamka", "poznamka pro me", "popis", "popis transakce",
        "nazev obchodnika", "obchodnik", "nazev protiuctu", "nazev partnera", "partner",
        "message", "note", "description", "merchant", "partner name", "typ transakce", "transaction type",
    )

    private val delimiters = listOf(';', ',', '\t')

    fun parse(text: String): List<StatementTransaction> {
        for (delimiter in delimiters) {
            val rows = readCsv(text, delimiter)
            val headerIndex = rows.indexOfFirst { row -> Columns.find(row) != null }
            if (headerIndex < 0) continue
            val columns = Columns.find(rows[headerIndex])!!
            return rows.drop(headerIndex + 1).mapNotNull { row -> columns.toTransaction(row, delimiter) }
        }
        throw StatementException(
            title = "Tohle není výpis z George",
            message = "V souboru chybí sloupce s datem a částkou. Exportujte pohyby z George jako CSV a zkuste to znovu.",
        )
    }

    private class Columns(
        val date: Int,
        val amount: Int,
        val currency: Int?,
        val originalAmount: Int?,
        val originalCurrency: Int?,
        val notes: List<Int>,
    ) {
        fun toTransaction(row: List<String>, delimiter: Char): StatementTransaction? {
            val date = row.getOrNull(date)?.let(::parseStatementDate) ?: return null
            val amount = row.getOrNull(amount)?.let(::parseStatementAmount) ?: return null
            if (amount >= 0) return null
            val spent = -amount
            val accountCurrency = currency?.let { row.getOrNull(it) }?.currencyCode() ?: BASE_CURRENCY

            val foreignCurrency = originalCurrency?.let { row.getOrNull(it) }?.currencyCode()
                ?.takeIf { it != accountCurrency }
            val foreignAmount = originalAmount?.let { row.getOrNull(it) }?.let(::parseStatementAmount)
                ?.let { kotlin.math.abs(it) }
                ?.takeIf { it > 0 }
            val isForeign = foreignCurrency != null && foreignAmount != null

            return StatementTransaction(
                date = date,
                amount = if (isForeign) foreignAmount else spent,
                currency = if (isForeign) foreignCurrency else accountCurrency,
                amountCzk = if (accountCurrency == BASE_CURRENCY) spent else null,
                note = notes.firstNotNullOfOrNull { row.getOrNull(it)?.trim()?.takeIf(String::isNotEmpty) }
                    ?: "Platba z výpisu",
                rawLine = row.joinToString(delimiter.toString()),
            )
        }

        companion object {
            /** Hlavička = řádek, ve kterém je sloupec s datem i s částkou. */
            fun find(header: List<String>): Columns? {
                val names = header.map(::normalizeHeader)
                val used = mutableSetOf<Int>()
                fun column(aliases: List<String>): Int? {
                    val index = aliases.firstNotNullOfOrNull { alias ->
                        names.indices.firstOrNull { it !in used && names[it] == alias }
                    } ?: aliases.firstNotNullOfOrNull { alias ->
                        names.indices.firstOrNull { it !in used && names[it].startsWith("$alias ") }
                    }
                    return index?.also { used += it }
                }

                // Nejdřív konkrétnější sloupce, ať si „částka“ nevezme „původní částku“
                val originalAmount = column(originalAmountColumns)
                val originalCurrency = column(originalCurrencyColumns)
                val date = column(dateColumns) ?: return null
                val amount = column(amountColumns) ?: return null
                val currency = column(currencyColumns)
                val notes = noteColumns.mapNotNull { alias -> column(listOf(alias)) }
                return Columns(date, amount, currency, originalAmount, originalCurrency, notes)
            }
        }
    }
}

/** "Částka (CZK)" → "castka czk": malá písmena, bez diakritiky a interpunkce. */
private fun normalizeHeader(name: String): String =
    Normalizer.normalize(name, Normalizer.Form.NFD)
        .replace(Regex("""\p{M}+"""), "")
        .lowercase()
        .replace(Regex("""[^a-z0-9]+"""), " ")
        .trim()

/** "CZK", "czk", " Kč " → ISO kód; nesmysl → null. */
private fun String.currencyCode(): String? {
    val code = trim().uppercase()
    return when {
        code == "KČ" || code == "KC" -> BASE_CURRENCY
        code.matches(Regex("[A-Z]{3}")) -> code
        else -> null
    }
}

/** Jednoduchá čtečka CSV: uvozovky, zdvojené uvozovky uvnitř hodnoty, zalomení řádku v uvozovkách. */
internal fun readCsv(text: String, delimiter: Char): List<List<String>> {
    val rows = mutableListOf<List<String>>()
    val row = mutableListOf<String>()
    val field = StringBuilder()
    var quoted = false
    var i = 0
    while (i < text.length) {
        val c = text[i]
        when {
            quoted && c == '"' && text.getOrNull(i + 1) == '"' -> { field.append('"'); i++ }
            c == '"' -> quoted = !quoted
            !quoted && c == delimiter -> { row += field.toString(); field.clear() }
            !quoted && (c == '\n' || c == '\r') -> {
                if (c == '\r' && text.getOrNull(i + 1) == '\n') i++
                row += field.toString()
                field.clear()
                if (row.any { it.isNotBlank() }) rows += row.toList()
                row.clear()
            }
            else -> field.append(c)
        }
        i++
    }
    row += field.toString()
    if (row.any { it.isNotBlank() }) rows += row.toList()
    return rows
}
