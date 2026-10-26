package com.example.financetracker.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.repository.CategoryRepository
import com.example.financetracker.data.repository.ExpenseRepository
import com.example.financetracker.ui.util.formatMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth

enum class PeriodType(val label: String) { Month("Měsíc"), Year("Rok") }

/** Zvolené období. [offset] 0 = aktuální, -1 = předchozí… */
data class Period(val type: PeriodType, val offset: Int) {
    val from: LocalDate
    val to: LocalDate
    val label: String

    init {
        when (type) {
            PeriodType.Month -> {
                val month = YearMonth.now().plusMonths(offset.toLong())
                from = month.atDay(1)
                to = month.atEndOfMonth()
                label = formatMonth(month)
            }
            PeriodType.Year -> {
                val year = Year.now().plusYears(offset.toLong())
                from = year.atDay(1)
                to = year.atMonth(12).atEndOfMonth()
                label = year.toString()
            }
        }
    }

    /** Do budoucnosti listovat nejde. */
    val canGoNext: Boolean get() = offset < 0
}

data class StatsUiState(
    val period: Period,
    val total: Double,
    val spending: List<CategorySpending>,
)

class StatsViewModel(
    expenseRepository: ExpenseRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {

    private val period = MutableStateFlow(Period(PeriodType.Month, 0))

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<StatsUiState?> = period
        .flatMapLatest { period ->
            combine(
                expenseRepository.observeCategoryTotals(period.from, period.to),
                categoryRepository.observeAll(),
            ) { totals, categories ->
                val spending = totals.withCategories(categories)
                StatsUiState(period = period, total = spending.sumOf { it.total }, spending = spending)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun selectType(type: PeriodType) = period.update { if (it.type == type) it else Period(type, 0) }

    fun previous() = period.update { it.copy(offset = it.offset - 1) }

    fun next() = period.update { if (it.canGoNext) it.copy(offset = it.offset + 1) else it }
}
