package com.example.financetracker.ui.navigation

import android.net.Uri
import android.os.Bundle
import com.example.financetracker.data.model.ExpenseSource
import java.time.LocalDate

/**
 * Všechny routy aplikace (podle HANDOFF.md §2).
 */
object Routes {
    // Záložky ve spodní liště
    const val DASHBOARD = "dashboard"
    const val EXPENSES = "expenses"
    const val STATS = "stats"
    const val SETTINGS = "settings"

    // Výdaj
    const val ADD = "add?currency={currency}&amount={amount}&note={note}&date={date}&source={source}"
    const val EXPENSE_DETAIL = "expense/{id}"
    const val EXPENSE_EDIT = "expense/{id}/edit"

    // Kategorie
    const val CATEGORIES = "categories"
    const val CATEGORY_EDIT = "categories/edit?id={id}"

    // Sken a import
    const val SCAN = "scan"
    const val IMPORT_GRAPH = "import"
    const val IMPORT_PICK = "import/pick"
    const val IMPORT_PREVIEW = "import/preview"

    /** Formulář nového výdaje, volitelně předvyplněný (např. po skenu QR Platby). */
    fun add(prefill: AddExpensePrefill = AddExpensePrefill()): String = buildString {
        append("add")
        val params = listOfNotNull(
            prefill.currency?.let { "currency=${Uri.encode(it)}" },
            prefill.amount?.let { "amount=$it" },
            prefill.note?.let { "note=${Uri.encode(it)}" },
            prefill.date?.let { "date=${it.toEpochDay()}" },
            prefill.source?.let { "source=${it.name}" },
        )
        if (params.isNotEmpty()) append("?").append(params.joinToString("&"))
    }

    fun expenseDetail(id: Long) = "expense/$id"
    fun expenseEdit(id: Long) = "expense/$id/edit"

    /** [id] null = nová kategorie. */
    fun categoryEdit(id: Long? = null) = if (id == null) "categories/edit" else "categories/edit?id=$id"
}

/**
 * Předvyplněné hodnoty formuláře nového výdaje.
 * Sken ji naplní a zavolá `navController.navigate(Routes.add(prefill))`.
 */
data class AddExpensePrefill(
    val amount: Double? = null,
    val currency: String? = null,
    val note: String? = null,
    val date: LocalDate? = null,
    val source: ExpenseSource? = null,
) {
    companion object {
        fun fromArguments(args: Bundle?): AddExpensePrefill = AddExpensePrefill(
            amount = args?.getString("amount")?.toDoubleOrNull(),
            currency = args?.getString("currency"),
            note = args?.getString("note"),
            date = args?.getString("date")?.toLongOrNull()?.let(LocalDate::ofEpochDay),
            source = args?.getString("source")?.let { runCatching { ExpenseSource.valueOf(it) }.getOrNull() },
        )
    }
}
