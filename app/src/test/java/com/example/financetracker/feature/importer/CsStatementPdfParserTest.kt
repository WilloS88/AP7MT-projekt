package com.example.financetracker.feature.importer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Text ve stejné podobě, jakou z PDF výpisu České spořitelny vrací PDFTextStripper (smyšlená data). */
class CsStatementPdfParserTest {

    private val statement = """
        Výpis z účtu
        Plus účet České spořitelny Období: 01.09.2026 - 30.09.2026
        Číslo účtu/kód banky: 1234567890/0800 Číslo výpisu: 009
        Majitel účtu: Novák Jan
        Měna účtu: CZK
        PŘEHLED POHYBŮ NA ÚČTU Počáteční zůstatek: 14 068.07
        Zaúčtováno Položka Číslo protiúčtu Variabilní symbol Částka
        Provedeno Název protiúčtu Konstantní symbol
        Popis
        01.09.2026 Platba kartou 30082026 -356.20
        1178
        4511612337
        XXXXXXXXXXXX1111 d.tran.30.08.2026 CZ ZLIN ALBERT VAM DEK
        CZK 356.20 d.zúč. 01.09.2026 částka v Kč: 356.20
        02.09.2026 Tuzemská odchozí úhrada 1190544010/3030 -1 000.00
        okamžitá
        02.09.2026
        02.09.2026 Příchozí úhrada 4512603372/5500 +70.00
        Jana Nováková
        02.09.2026
        Pokračování na další straně
        Česká spořitelna, a.s., Praha 4, Olbrachtova 1929/62, PSČ 140 00, IČO: 452 44 782,
        strana 1/2
        Výpis z účtu
        Zaúčtováno Položka Číslo protiúčtu Variabilní symbol Částka
        14.09.2026 Tuzemská odchozí úhrada 2101800014/3030 -300.00
        okamžitá
        Doplatek za fakturu
        13.09.2026
        16.09.2026 Platba kartou 14092026 -251.23
        1178
        4511612337
        XXXXXXXXXXXX1111 d.tran.14.09.2026 AT WIEN BILLA DANKT
        EUR 10.00 d.zúč. 16.09.2026 částka v Kč: 251.23
        30.09.2026 Ceny za služby
        Cena za vedení účtu 0.00
        Konečný zůstatek: 9 672.82
    """.trimIndent()

    @Test
    fun recognizesCsStatement() {
        assertTrue(CsStatementPdfParser.recognizes(statement))
        assertFalse(CsStatementPdfParser.recognizes("Faktura č. 2026001"))
    }

    @Test
    fun keepsOnlyExpenses() {
        val result = CsStatementPdfParser.parse(statement)
        assertEquals(4, result.size)
        assertTrue(result.none { it.note.contains("Nováková") })
    }

    @Test
    fun cardPaymentUsesTransactionDateAndMerchant() {
        val card = CsStatementPdfParser.parse(statement).first()
        assertEquals(LocalDate.of(2026, 8, 30), card.date)
        assertEquals(356.20, card.amount, 0.001)
        assertEquals("CZK", card.currency)
        assertEquals(356.20, card.amountCzk!!, 0.001)
        assertEquals("ZLIN ALBERT VAM DEK", card.note)
        assertTrue(card.rawLine.startsWith("01.09.2026 Platba kartou"))
    }

    @Test
    fun cardMerchantWithoutPaddedCity() {
        // PDFBox nechává mezi městem a obchodníkem zarovnávací mezery
        val padded = statement.replace("CZ ZLIN ALBERT VAM DEK", "CZ ZLIN          ALBERT VAM DEK")
        assertEquals("ALBERT VAM DEK", CsStatementPdfParser.parse(padded).first().note)
    }

    @Test
    fun transferWithoutMessageNamesCounterAccount() {
        val transfer = CsStatementPdfParser.parse(statement)[1]
        assertEquals(1000.0, transfer.amount, 0.001)
        assertEquals(LocalDate.of(2026, 9, 2), transfer.date)
        assertEquals("Tuzemská odchozí úhrada: 1190544010/3030", transfer.note)
    }

    @Test
    fun transferMessageAndExecutionDateAcrossPageBreak() {
        val transfer = CsStatementPdfParser.parse(statement)[2]
        assertEquals(300.0, transfer.amount, 0.001)
        assertEquals(LocalDate.of(2026, 9, 13), transfer.date)
        assertEquals("Tuzemská odchozí úhrada: Doplatek za fakturu", transfer.note)
    }

    @Test
    fun foreignCardPaymentKeepsOriginalCurrencyAndCzkAmount() {
        val foreign = CsStatementPdfParser.parse(statement)[3]
        assertEquals("EUR", foreign.currency)
        assertEquals(10.0, foreign.amount, 0.001)
        assertEquals(251.23, foreign.amountCzk!!, 0.001)
        assertEquals("WIEN BILLA DANKT", foreign.note)
    }

    @Test
    fun serviceFeeLineDoesNotLeakIntoPreviousPayment() {
        val last = CsStatementPdfParser.parse(statement).last()
        assertFalse(last.rawLine.contains("Ceny za služby"))
    }

    @Test
    fun eurAccountHasNoCzkAmount() {
        val eur = """
            Výpis z účtu České spořitelny
            Měna účtu: EUR
            05.09.2026 Platba kartou 3092026 -12.40
            XXXXXXXXXXXX1111 d.tran.03.09.2026 SK BRATISLAVA LIDL
            EUR 12.40 d.zúč. 05.09.2026
        """.trimIndent()
        val payment = CsStatementPdfParser.parse(eur).single()
        assertEquals("EUR", payment.currency)
        assertEquals(12.40, payment.amount, 0.001)
        assertNull(payment.amountCzk)
    }

    @Test
    fun parsesAmountsAndDates() {
        assertEquals(-5000.0, parseStatementAmount("-5 000.00")!!, 0.0)
        assertEquals(-1234.56, parseStatementAmount("−1 234,56 Kč")!!, 0.0)
        assertEquals(1234.56, parseStatementAmount("1,234.56")!!, 0.0)
        assertEquals(1234.0, parseStatementAmount("1 234")!!, 0.0)
        assertEquals(-7.5, parseStatementAmount("\"-7,5\"")!!, 0.0)
        assertNull(parseStatementAmount("abc"))
        assertEquals(LocalDate.of(2026, 9, 1), parseStatementDate("01.09.2026"))
        assertEquals(LocalDate.of(2026, 9, 1), parseStatementDate("1. 9. 2026 14:05"))
        assertEquals(LocalDate.of(2026, 9, 1), parseStatementDate("2026-09-01"))
        assertNull(parseStatementDate("31.02.2026"))
    }
}
