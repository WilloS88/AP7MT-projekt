package com.example.financetracker.feature.importer

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

enum class StatementFormat { CSV, PDF }

/** Načtený soubor výpisu. */
data class ParsedStatement(
    val fileName: String,
    val format: StatementFormat,
    val transactions: List<StatementTransaction>,
)

/** Přečte vybraný soubor (CSV nebo PDF výpis České spořitelny) a vrátí výdaje v něm. */
class StatementReader(context: Context) {

    private val context = context.applicationContext

    suspend fun read(uri: Uri): ParsedStatement = withContext(Dispatchers.IO) {
        val fileName = displayName(uri) ?: "Výpis"
        val bytes = try {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (e: IOException) {
            null
        } ?: throw StatementException("Soubor nejde otevřít", "Zkuste ho vybrat znovu, případně ho nejdřív stáhněte do telefonu.")

        val isPdf = bytes.startsWith(PdfSignature) ||
            context.contentResolver.getType(uri) == "application/pdf" ||
            fileName.endsWith(".pdf", ignoreCase = true)

        val transactions = if (isPdf) parsePdf(bytes) else CsStatementCsvParser.parse(decodeStatementText(bytes))
        if (transactions.isEmpty()) {
            throw StatementException("Ve výpisu nejsou žádné výdaje", "Soubor jsme přečetli, ale nenašli jsme v něm žádnou odchozí platbu.")
        }
        ParsedStatement(fileName, if (isPdf) StatementFormat.PDF else StatementFormat.CSV, transactions)
    }

    private fun parsePdf(bytes: ByteArray): List<StatementTransaction> {
        PDFBoxResourceLoader.init(context)
        val text = try {
            PDDocument.load(bytes).use { document ->
                PDFTextStripper().apply { sortByPosition = true }.getText(document)
            }
        } catch (e: InvalidPasswordException) {
            throw StatementException("PDF je zaheslované", "Uložte výpis bez hesla a zkuste to znovu.")
        } catch (e: IOException) {
            throw StatementException("PDF nejde přečíst", "Soubor je poškozený nebo to není PDF.")
        }
        if (text.isBlank()) {
            throw StatementException(
                "PDF neobsahuje text",
                "Nejspíš jde o naskenovaný výpis. Stáhněte v George elektronický výpis (PDF) nebo export pohybů v CSV.",
            )
        }
        if (!CsStatementPdfParser.recognizes(text)) {
            throw StatementException(
                "Tohle není výpis České spořitelny",
                "Zatím umíme jen výpisy z účtu České spořitelny. Vyberte jiný soubor.",
            )
        }
        return CsStatementPdfParser.parse(text)
    }

    private fun displayName(uri: Uri): String? = runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }.getOrNull()

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean =
        size >= prefix.size && prefix.indices.all { this[it] == prefix[it] }

    private companion object {
        val PdfSignature = "%PDF".toByteArray()
    }
}
