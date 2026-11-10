package com.example.financetracker.feature.expense

import androidx.compose.runtime.Composable
import com.example.financetracker.ui.navigation.AddExpensePrefill

// Route: add?currency=&amount=&note=&date=&source=
// [prefill] = předvyplněné hodnoty (např. ze skenu QR Platby).
@Composable
fun AddExpenseScreen(
    prefill: AddExpensePrefill,
    onBack: () -> Unit,
    onScan: () -> Unit,
    onSaved: () -> Unit,
) {
    ExpenseForm(
        title = "Nový výdaj",
        viewModel = expenseFormViewModel(editId = null, prefill = prefill),
        onBack = onBack,
        onSaved = onSaved,
        onScan = onScan,
    )
}
