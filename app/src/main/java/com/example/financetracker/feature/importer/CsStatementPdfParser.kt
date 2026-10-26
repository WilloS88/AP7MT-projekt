package com.example.financetracker.feature.importer

import com.example.financetracker.data.repository.BASE_CURRENCY

/**
 * Text měsíčního PDF výpisu České spořitelny (z PDFTextStripper) → výdaje.
 *
 * Každý pohyb začíná řádkem `dd.MM.yyyy <položka> <protiúčet / symboly> <±částka>`,
 * pod ním jsou řádky s podrobnostmi až do dalšího pohybu nebo konce strany. U karetní platby:
 * ```
 * 01.09.2026 Platba kartou 30082026 -356.20
 * 1178
 * 4511612337
 * XXXXXXXXXXXX2337 d.tran.30.08.2026 CZ ZLIN ALBERT VAM DEK
 * CZK 356.20 d.zúč. 01.09.2026 částka v Kč: 356.20
 * ```
 * Bere se jen záporná částka (výdaj). Datum je den transakce (`d.tran.` / „Provedeno“),
 * ne den zaúčtování.
 */
object CsStatementPdfParser {

    private const val Amount = """\d{1,3}(?:[  ]\d{3})*[.,]\d{2}"""
    private val movementLine = Regex("""^(\d{2}\.\d{2}\.\d{4})\s+(.+?)\s+([+-]\s?$Amount)$""")
    private val newItemLine = Regex("""^\d{2}\.\d{2}\.\d{4}\s+\D""")
    private val dateOnlyLine = Regex("""^\d{2}\.\d{2}\.\d{4}$""")
    private val numberOnlyLine = Regex("""^[\d\s/-]+$""")
    private val cardDetail = Regex("""d\.tran\.(\d{2}\.\d{2}\.\d{4})\s+(?:[A-Z]{2}\s+)?(.+)$""")
    private val foreignAmount = Regex("""^([A-Z]{3})\s+($Amount)\s+d\.zúč\.""")
    private val accountCurrency = Regex("""Měna účtu:\s*([A-Z]{3})""")
    private val columnGap = Regex("""\s{2,}""")
    private val whitespace = Regex("""\s+""")

    /** Konec bloku pohybů na straně. */
    private val stopMarkers = listOf("Pokračování na další straně", "Konečný zůstatek", "Česká spořitelna, a.s.")

    /** Je to výpis České spořitelny? (Jiné PDF se odmítne s jasnou hláškou.) */
    fun recognizes(text: String): Boolean =
        text.contains("Výpis z účtu") && (text.contains("Česká spořitelna") || text.contains("České spořitelny"))

    fun parse(text: String): List<StatementTransaction> {
        val currency = accountCurrency.find(text)?.groupValues?.get(1) ?: BASE_CURRENCY
        val result = mutableListOf<StatementTransaction>()
        var header: String? = null
        val details = mutableListOf<String>()

        fun flush() {
            header?.let { line -> toTransaction(line, details, currency)?.let(result::add) }
            header = null
            details.clear()
        }

        text.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.forEach { line ->
            when {
                movementLine.matches(line) -> {
                    flush()
                    header = line
                }
                newItemLine.containsMatchIn(line) || stopMarkers.any { line.startsWith(it) } -> flush()
                header != null -> details += line
            }
        }
        flush()
        return result
    }

    private fun toTransaction(header: String, details: List<String>, accountCurrency: String): StatementTransaction? {
        val match = movementLine.matchEntire(header) ?: return null
        val (postedDate, item, amountText) = match.destructured
        val amount = parseStatementAmount(amountText) ?: return null
        if (amount >= 0) return null
        val spent = -amount

        // "Platba kartou 30082026" → položka "Platba kartou", zbytek = protiúčet / symboly
        val words = item.split(' ')
        val itemWords = words.takeWhile { word -> word.none(Char::isDigit) }
        val itemName = itemWords.joinToString(" ")
        val counterparty = words.getOrNull(itemWords.size)

        val card = details.firstNotNullOfOrNull { cardDetail.find(it) }
        val foreign = details.firstNotNullOfOrNull { foreignAmount.find(it) }
            ?.takeIf { it.groupValues[1] != accountCurrency }
        val foreignCurrency = foreign?.groupValues?.get(1)
        val foreignValue = foreign?.groupValues?.get(2)?.let(::parseStatementAmount)
        val executedDate = card?.groupValues?.get(1) ?: details.firstOrNull { dateOnlyLine.matches(it) } ?: postedDate

        val note = when {
            // Město a obchodník jsou v PDF sloupce zarovnané mezerami: "ZLIN      ALBERT VAM DEK"
            card != null -> card.groupValues[2].trim().split(columnGap).last()
            else -> {
                val message = details.firstOrNull { line ->
                    !dateOnlyLine.matches(line) && !numberOnlyLine.matches(line) && line != "okamžitá"
                }
                listOfNotNull(itemName.ifEmpty { null }, message ?: counterparty).joinToString(": ")
            }
        }.replace(whitespace, " ").ifEmpty { "Platba z výpisu" }

        val isForeign = foreignCurrency != null && foreignValue != null
        return StatementTransaction(
            date = parseStatementDate(executedDate) ?: parseStatementDate(postedDate) ?: return null,
            amount = if (isForeign) foreignValue else spent,
            currency = if (isForeign) foreignCurrency else accountCurrency,
            amountCzk = if (accountCurrency == BASE_CURRENCY) spent else null,
            note = note,
            rawLine = (listOf(header) + details).joinToString("\n"),
        )
    }
}
