package com.example.financetracker.feature.stats

import com.example.financetracker.data.model.Category
import com.example.financetracker.data.model.CategoryTotal
import com.example.financetracker.ui.theme.DefaultCategories

/** Útrata v jedné kategorii za období. [share] = podíl na celkové útratě (0–1). */
data class CategorySpending(
    val category: Category,
    val total: Double,
    val share: Float,
)

/** Součty z databáze doplní o kategorie a podíly. Pořadí zůstává (od největší útraty). */
fun List<CategoryTotal>.withCategories(categories: List<Category>): List<CategorySpending> {
    val byId = categories.associateBy { it.id }
    val positive = filter { it.total > 0 }
    val sum = positive.sumOf { it.total }
    return positive.map { total ->
        CategorySpending(
            category = byId[total.categoryId] ?: unknownCategory(total.categoryId),
            total = total.total,
            share = (total.total / sum).toFloat(),
        )
    }
}

private fun unknownCategory(id: Long) = Category(
    id = id,
    name = "Bez kategorie",
    iconKey = "other",
    colorSlot = DefaultCategories.Ostatni.id,
)
