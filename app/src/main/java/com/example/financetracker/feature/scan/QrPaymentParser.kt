package com.example.financetracker.feature.scan

import com.example.financetracker.data.model.ExpenseSource
import com.example.financetracker.data.repository.BASE_CURRENCY
import com.example.financetracker.ui.navigation.AddExpensePrefill
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Údaje z QR Platby (formát SPAYD, např. `SPD*1.0*ACC:CZ…*AM:450.00*CC:CZK*MSG:Obed`). */
data class QrPayment(
    /** IBAN příjemce, případně s BIC za `+`. */
    val account: String,
    val amount: Double?,
    /** ISO kód měny, bez CC je platba v Kč. */
    val currency: String,
    val message: String?,
    val recipientName: String?,
    val variableSymbol: String?,
    val dueDate: LocalDate?,
)

/**
 * Parser QR Platby podle specifikace SPAYD (České bankovní asociace).
 *
 * Řetězec začíná hlavičkou `SPD*<verze>*`, dál jsou pole `KLÍČ:hodnota` oddělená `*`.
 * Hvězdička uvnitř hodnoty je zapsaná jako `%2A`. Povinný je jen účet (ACC).
 */
object QrPaymentParser {

    private val dateFormat = DateTimeFormatter.BASIC_ISO_DATE // 20261026

    /** Vrací null, když text není QR Platba (jiný QR kód, chybí účet, nesmyslná částka). */
    fun parse(text: String): QrPayment? {
        val parts = text.trim().split('*')
        if (parts.size < 3 || !parts[0].equals("SPD", ignoreCase = true)) return null
        if (!parts[1].matches(Regex("""\d+\.\d+"""))) return null

        val fields = parts.drop(2)
            .filter { ':' in it }
            .associate { field ->
                val key = field.substringBefore(':').uppercase()
                key to field.substringAfter(':').replace("%2A", "*", ignoreCase = true).trim()
            }

        val account = fields["ACC"]?.takeIf { it.isNotEmpty() } ?: return null
        val amount = fields["AM"]?.let { it.toDoubleOrNull()?.takeIf { value -> value > 0 } ?: return null }
        val currency = fields["CC"]?.uppercase()?.takeIf { it.matches(Regex("[A-Z]{3}")) } ?: BASE_CURRENCY

        return QrPayment(
            account = account,
            amount = amount,
            currency = currency,
            message = fields["MSG"]?.takeIf { it.isNotEmpty() },
            recipientName = fields["RN"]?.takeIf { it.isNotEmpty() },
            variableSymbol = fields["X-VS"]?.takeIf { it.isNotEmpty() },
            dueDate = fields["DT"]?.let { runCatching { LocalDate.parse(it, dateFormat) }.getOrNull() },
        )
    }
}

/** Druh přečteného kódu – rozhoduje, jak se obsah převede na výdaj. */
enum class ScannedCodeType { QR, PRODUCT_BARCODE }

/**
 * Převede přečtený kód na předvyplněný formulář výdaje.
 * - QR Platba → částka, měna, poznámka (zpráva, jinak jméno příjemce).
 * - Čárový kód zboží (EAN/UPC) → číslo kódu do poznámky, částku doplní uživatel.
 * - Jiný QR kód → null (obrazovka ukáže chybu).
 */
fun prefillFromScannedCode(rawValue: String, type: ScannedCodeType): AddExpensePrefill? = when (type) {
    ScannedCodeType.PRODUCT_BARCODE -> rawValue.trim().takeIf { it.isNotEmpty() }?.let { code ->
        AddExpensePrefill(note = "EAN $code", source = ExpenseSource.QR)
    }
    ScannedCodeType.QR -> QrPaymentParser.parse(rawValue)?.let { payment ->
        AddExpensePrefill(
            amount = payment.amount,
            currency = payment.currency,
            note = payment.message ?: payment.recipientName ?: "QR platba",
            source = ExpenseSource.QR,
        )
    }
}
