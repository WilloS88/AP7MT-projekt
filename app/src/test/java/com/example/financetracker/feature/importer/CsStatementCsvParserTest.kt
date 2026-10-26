package com.example.financetracker.feature.importer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDate

class CsStatementCsvParserTest {

    @Test
    fun parsesGeorgeExportWithSemicolons() {
        val csv = """
            "Datum zaúčtování";"Datum provedení";"Název protiúčtu";"Číslo účtu protiúčtu";"Částka";"Měna";"Zpráva pro příjemce";"Poznámka"
            "02.09.2026";"31.08.2026";"Alza.cz a.s.";"";"-929,00";"CZK";"";"Sluchátka"
            "02.09.2026";"02.09.2026";"Jana Nováková";"4512603372/5500";"70,00";"CZK";"Vrácení";""
            "04.09.2026";"04.09.2026";"";"1190544010/3030";"-1 400,50";"CZK";"Nájem září";""
        """.trimIndent()

        val result = CsStatementCsvParser.parse(csv)

        assertEquals(2, result.size)
        with(result[0]) {
            assertEquals(LocalDate.of(2026, 8, 31), date)
            assertEquals(929.0, amount, 0.0)
            assertEquals("CZK", currency)
            assertEquals(929.0, amountCzk!!, 0.0)
            assertEquals("Sluchátka", note)
        }
        with(result[1]) {
            assertEquals(1400.5, amount, 0.0)
            assertEquals("Nájem září", note)
        }
    }

    @Test
    fun findsColumnsInAnyOrderAndSkipsPreamble() {
        val csv = """
            Pohyby na účtu 1234567890/0800
            Měna,Partner,Částka,Datum
            CZK,"Lidl, Zlín","-11.90",2026-09-02
        """.trimIndent()

        val row = CsStatementCsvParser.parse(csv).single()
        assertEquals(LocalDate.of(2026, 9, 2), row.date)
        assertEquals(11.9, row.amount, 0.0)
        assertEquals("Lidl, Zlín", row.note)
    }

    @Test
    fun foreignCardPaymentKeepsOriginalAmount() {
        val csv = """
            Datum provedení;Částka;Měna;Původní částka;Původní měna;Název obchodníka
            14.09.2026;-251,23;CZK;-10,00;EUR;BILLA WIEN
        """.trimIndent()

        val row = CsStatementCsvParser.parse(csv).single()
        assertEquals("EUR", row.currency)
        assertEquals(10.0, row.amount, 0.0)
        assertEquals(251.23, row.amountCzk!!, 0.0)
        assertEquals("BILLA WIEN", row.note)
    }

    @Test
    fun foreignAccountNeedsConversion() {
        val csv = "Datum;Částka;Měna\n01.09.2026;-12,40;EUR\n"
        val row = CsStatementCsvParser.parse(csv).single()
        assertEquals("EUR", row.currency)
        assertNull(row.amountCzk)
        assertEquals("Platba z výpisu", row.note)
    }

    @Test
    fun englishHeadersAndTabs() {
        val csv = "Booking date\tAmount\tCurrency\tPartner name\n01/09/2026\t-50.00\tCZK\tRegioJet\n"
        val row = CsStatementCsvParser.parse(csv).single()
        assertEquals(LocalDate.of(2026, 9, 1), row.date)
        assertEquals("RegioJet", row.note)
    }

    @Test
    fun skipsBrokenRows() {
        val csv = "Datum;Částka\nnesmysl;-10\n01.09.2026;abc\n01.09.2026;-10\n"
        assertEquals(1, CsStatementCsvParser.parse(csv).size)
    }

    @Test
    fun rejectsFileWithoutHeader() {
        assertThrows(StatementException::class.java) { CsStatementCsvParser.parse("ahoj;svete\n1;2") }
    }

    @Test
    fun readsQuotedFieldsWithNewlinesAndQuotes() {
        val rows = readCsv("a;\"b \"\"c\"\"\nd\";e\r\nf;g", ';')
        assertEquals(listOf(listOf("a", "b \"c\"\nd", "e"), listOf("f", "g")), rows)
    }

    @Test
    fun decodesWindows1250AndUtf8Bom() {
        val text = "Částka;Měna"
        assertEquals(text, decodeStatementText(text.toByteArray(charset("windows-1250"))))
        assertEquals(text, decodeStatementText(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + text.toByteArray()))
    }
}
