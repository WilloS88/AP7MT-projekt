package com.example.financetracker.feature.expense

import androidx.compose.runtime.Composable

// Route: expense/{id}/edit
@Composable
fun EditExpenseScreen(
    expenseId: Long,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    ExpenseForm(
        title = "Upravit výdaj",
        viewModel = expenseFormViewModel(editId = expenseId),
        onBack = onBack,
        onSaved = onSaved,
    )
}
