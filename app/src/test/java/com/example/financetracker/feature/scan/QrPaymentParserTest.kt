package com.example.financetracker.feature.scan

import com.example.financetracker.data.model.ExpenseSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class QrPaymentParserTest {

    @Test
    fun parsesFullPayment() {
        val payment = QrPaymentParser.parse(
            "SPD*1.0*ACC:CZ5855000000001265098001+RZBCCZPP*AM:450.00*CC:CZK*MSG:Obed s kolegy" +
                "*RN:Restaurace U Lipy*X-VS:1234567890*DT:20261026",
        )!!

        assertEquals("CZ5855000000001265098001+RZBCCZPP", payment.account)
        assertEquals(450.0, payment.amount!!, 0.0)
        assertEquals("CZK", payment.currency)
        assertEquals("Obed s kolegy", payment.message)
        assertEquals("Restaurace U Lipy", payment.recipientName)
        assertEquals("1234567890", payment.variableSymbol)
        assertEquals(LocalDate.of(2026, 10, 26), payment.dueDate)
    }

    @Test
    fun missingCurrencyMeansCzk() {
        val payment = QrPaymentParser.parse("SPD*1.0*ACC:CZ5855000000001265098001*AM:99.5")!!
        assertEquals("CZK", payment.currency)
        assertEquals(99.5, payment.amount!!, 0.0)
    }

    @Test
    fun amountIsOptional() {
        val payment = QrPaymentParser.parse("SPD*1.0*ACC:CZ5855000000001265098001*MSG:Dar")!!
        assertNull(payment.amount)
        assertEquals("Dar", payment.message)
    }

    @Test
    fun foreignCurrencyAndLowercaseValues() {
        val payment = QrPaymentParser.parse("spd*1.0*ACC:CZ5855000000001265098001*AM:12.50*CC:eur")!!
        assertEquals("EUR", payment.currency)
        assertEquals(12.5, payment.amount!!, 0.0)
    }

    @Test
    fun decodesEscapedAsterisk() {
        val payment = QrPaymentParser.parse("SPD*1.0*ACC:CZ5855000000001265098001*MSG:Sleva 5%2A2")!!
        assertEquals("Sleva 5*2", payment.message)
    }

    @Test
    fun trailingAsteriskAndWhitespaceAreIgnored() {
        val payment = QrPaymentParser.parse("  SPD*1.0*ACC:CZ5855000000001265098001*AM:10*  \n")!!
        assertEquals(10.0, payment.amount!!, 0.0)
    }

    @Test
    fun rejectsOtherCodes() {
        assertNull(QrPaymentParser.parse("https://example.com"))
        assertNull(QrPaymentParser.parse("SPD*1.0*AM:450.00*CC:CZK")) // bez účtu
        assertNull(QrPaymentParser.parse("SPD*abc*ACC:CZ5855000000001265098001"))
        assertNull(QrPaymentParser.parse("SPD*1.0*ACC:CZ5855000000001265098001*AM:abc"))
        assertNull(QrPaymentParser.parse("SPD*1.0*ACC:CZ5855000000001265098001*AM:-5"))
        assertNull(QrPaymentParser.parse(""))
    }

    @Test
    fun qrPaymentBecomesPrefill() {
        val prefill = prefillFromScannedCode(
            "SPD*1.0*ACC:CZ5855000000001265098001*AM:450.00*CC:CZK*MSG:Obed",
            ScannedCodeType.QR,
        )!!
        assertEquals(450.0, prefill.amount!!, 0.0)
        assertEquals("CZK", prefill.currency)
        assertEquals("Obed", prefill.note)
        assertEquals(ExpenseSource.QR, prefill.source)
    }

    @Test
    fun prefillNoteFallsBackToRecipient() {
        val withName = prefillFromScannedCode("SPD*1.0*ACC:CZ5855000000001265098001*RN:Elektrina s.r.o.", ScannedCodeType.QR)!!
        assertEquals("Elektrina s.r.o.", withName.note)

        val bare = prefillFromScannedCode("SPD*1.0*ACC:CZ5855000000001265098001", ScannedCodeType.QR)!!
        assertEquals("QR platba", bare.note)
    }

    @Test
    fun barcodeGoesToNote() {
        val prefill = prefillFromScannedCode("8594001021234", ScannedCodeType.PRODUCT_BARCODE)!!
        assertEquals("EAN 8594001021234", prefill.note)
        assertNull(prefill.amount)
        assertEquals(ExpenseSource.QR, prefill.source)
    }

    @Test
    fun nonPaymentQrGivesNoPrefill() {
        assertNull(prefillFromScannedCode("https://example.com", ScannedCodeType.QR))
    }
}
