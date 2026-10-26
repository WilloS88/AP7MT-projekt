package com.example.financetracker.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.model.Category
import com.example.financetracker.data.model.Expense
import com.example.financetracker.data.repository.CategoryRepository
import com.example.financetracker.data.repository.ExpenseRepository
import com.example.financetracker.feature.stats.CategorySpending
import com.example.financetracker.feature.stats.withCategories
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth

data class DashboardUiState(
    val month: YearMonth,
    val monthTotal: Double,
    val spending: List<CategorySpending>,
    val recent: List<Expense>,
    val categories: Map<Long, Category>,
)

class DashboardViewModel(
    expenseRepository: ExpenseRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {

    private val month = YearMonth.now()
    private val from = month.atDay(1)
    private val to = month.atEndOfMonth()

    val state: StateFlow<DashboardUiState?> = combine(
        expenseRepository.observeTotalBetween(from, to),
        expenseRepository.observeCategoryTotals(from, to),
        expenseRepository.observeAll().map { it.take(RecentCount) },
        categoryRepository.observeAll(),
    ) { total, totals, recent, categories ->
        DashboardUiState(
            month = month,
            monthTotal = total,
            spending = totals.withCategories(categories),
            recent = recent,
            categories = categories.associateBy { it.id },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private companion object {
        const val RecentCount = 5
    }
}
