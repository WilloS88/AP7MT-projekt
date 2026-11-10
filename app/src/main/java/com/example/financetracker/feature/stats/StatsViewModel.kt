package com.example.financetracker.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.model.Category
import com.example.financetracker.data.model.Expense
import com.example.financetracker.data.repository.CategoryRepository
import com.example.financetracker.data.repository.ExpenseRepository
import com.example.financetracker.ui.util.formatDay
import com.example.financetracker.ui.util.formatMonth
import com.example.financetracker.ui.util.formatWeek
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

/**
 * Délka období ve statistikách.
 * [showsExpenses] = pod grafem se ukáže i seznam jednotlivých výdajů (u krátkých období).
 */
enum class PeriodType(val label: String, val emptyMessage: String, val showsExpenses: Boolean) {
    Day("Den", "Tento den jste nic neutratili.", showsExpenses = true),
    Week("Týden", "Tento týden jste nic neutratili.", showsExpenses = true),
    Month("Měsíc", "V tomto měsíci zatím nemáte žádné výdaje.", showsExpenses = false),
    Year("Rok", "V tomto roce zatím nemáte žádné výdaje.", showsExpenses = false),
}

/**
 * Zvolené období. [offset] 0 = aktuální, -1 = předchozí…
 * Týden začíná v pondělí. [today] jde nastavit kvůli testům.
 */
data class Period(
    val type: PeriodType,
    val offset: Int,
    val today: LocalDate = LocalDate.now(),
) {
    val from: LocalDate
    val to: LocalDate
    val label: String

    init {
        when (type) {
            PeriodType.Day -> {
                from = today.plusDays(offset.toLong())
                to = from
                label = formatDay(from, today)
            }
            PeriodType.Week -> {
                from = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks(offset.toLong())
                to = from.plusDays(6)
                label = formatWeek(from, to)
            }
            PeriodType.Month -> {
                val month = YearMonth.from(today).plusMonths(offset.toLong())
                from = month.atDay(1)
                to = month.atEndOfMonth()
                label = formatMonth(month)
            }
            PeriodType.Year -> {
                val year = Year.from(today).plusYears(offset.toLong())
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
    /** Jednotlivé výdaje období – jen u dne a týdne, jinak prázdné. */
    val expenses: List<Expense>,
    val categoriesById: Map<Long, Category>,
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
                if (period.type.showsExpenses) {
                    expenseRepository.observeBetween(period.from, period.to)
                } else {
                    flowOf(emptyList())
                },
            ) { totals, categories, expenses ->
                val spending = totals.withCategories(categories)
                StatsUiState(
                    period = period,
                    total = spending.sumOf { it.total },
                    spending = spending,
                    expenses = expenses,
                    categoriesById = categories.associateBy { it.id },
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun selectType(type: PeriodType) = period.update { if (it.type == type) it else Period(type, 0) }

    fun previous() = period.update { it.copy(offset = it.offset - 1) }

    fun next() = period.update { if (it.canGoNext) it.copy(offset = it.offset + 1) else it }
}
