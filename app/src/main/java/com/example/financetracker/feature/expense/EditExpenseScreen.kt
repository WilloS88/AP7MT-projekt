package com.example.financetracker.feature.expense

import androidx.compose.runtime.Composable
import com.example.financetracker.ui.navigation.PlaceholderScreen

// Dočasná obrazovka. Nahradit obsah funkce skutečnou obrazovkou, parametry ponechat.
// Route: expense/{id}/edit. Formulář sdílet s AddExpenseScreen.
@Composable
fun EditExpenseScreen(
    expenseId: Long,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    PlaceholderScreen(
        title = "Upravit výdaj #$expenseId",
        onBack = onBack,
        actions = listOf("Uložit" to onSaved),
    )
}
