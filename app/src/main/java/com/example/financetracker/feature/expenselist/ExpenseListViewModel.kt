package com.example.financetracker.feature.expenselist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.model.Category
import com.example.financetracker.data.model.Expense
import com.example.financetracker.data.repository.CategoryRepository
import com.example.financetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate

/** Výdaje jednoho dne. */
data class DayGroup(
    val date: LocalDate,
    val total: Double,
    val expenses: List<Expense>,
)

data class ExpenseListUiState(
    val query: String,
    val categories: List<Category>,
    val selectedCategoryIds: Set<Long>,
    val days: List<DayGroup>,
    /** Kolik výdajů je celkem (bez filtru) – 0 = ještě žádný výdaj. */
    val allCount: Int,
) {
    val categoriesById: Map<Long, Category> = categories.associateBy { it.id }
    val isFiltered: Boolean get() = query.isNotBlank() || selectedCategoryIds.isNotEmpty()
}

class ExpenseListViewModel(
    expenseRepository: ExpenseRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {

    /** Text hledání drží Compose state (ne Flow), aby pole při rychlém psaní necukalo. */
    var query by mutableStateOf("")
        private set
    private val selectedCategoryIds = MutableStateFlow(emptySet<Long>())

    val state: StateFlow<ExpenseListUiState?> = combine(
        expenseRepository.observeAll(),
        categoryRepository.observeAll(),
        snapshotFlow { query },
        selectedCategoryIds,
    ) { expenses, categories, query, selected ->
        val byId = categories.associateBy { it.id }
        val filtered = expenses.filter { expense ->
            (selected.isEmpty() || expense.categoryId in selected) && expense.matches(query, byId[expense.categoryId])
        }
        ExpenseListUiState(
            query = query,
            categories = categories,
            selectedCategoryIds = selected,
            // Výdaje přichází seřazené od nejnovějšího, groupBy pořadí zachová
            days = filtered.groupBy { it.date }.map { (date, items) ->
                DayGroup(date = date, total = items.sumOf { it.amountCzk }, expenses = items)
            },
            allCount = expenses.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun onQueryChange(value: String) {
        query = value
    }

    fun toggleCategory(id: Long) = selectedCategoryIds.update { if (id in it) it - id else it + id }

    fun clearFilters() {
        query = ""
        selectedCategoryIds.value = emptySet()
    }
}

/** Hledá v poznámce a v názvu kategorie, bez ohledu na velikost písmen. */
private fun Expense.matches(query: String, category: Category?): Boolean {
    val text = query.trim()
    if (text.isEmpty()) return true
    return note.orEmpty().contains(text, ignoreCase = true) ||
        category?.name.orEmpty().contains(text, ignoreCase = true)
}
