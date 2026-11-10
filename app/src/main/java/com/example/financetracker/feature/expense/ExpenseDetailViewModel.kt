package com.example.financetracker.feature.expense

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.model.Category
import com.example.financetracker.data.model.Expense
import com.example.financetracker.data.repository.CategoryRepository
import com.example.financetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** [expense] null = výdaj neexistuje (nebo byl smazán). */
data class ExpenseDetailState(
    val expense: Expense?,
    val category: Category?,
)

class ExpenseDetailViewModel(
    expenseId: Long,
    private val expenseRepository: ExpenseRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {

    /** null = ještě se načítá. */
    val state: StateFlow<ExpenseDetailState?> = combine(
        expenseRepository.observe(expenseId),
        categoryRepository.observeAll(),
    ) { expense, categories ->
        ExpenseDetailState(expense, categories.firstOrNull { it.id == expense?.categoryId })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** true = probíhá mazání (místo „výdaj nenalezen“ se ukáže načítání). */
    var deleting by mutableStateOf(false)
        private set

    /** true = smazáno → obrazovka se zavře. */
    var deleted by mutableStateOf(false)
        private set

    fun delete() {
        val expense = state.value?.expense ?: return
        if (deleting) return
        deleting = true
        viewModelScope.launch {
            expenseRepository.delete(expense)
            deleted = true
        }
    }
}
