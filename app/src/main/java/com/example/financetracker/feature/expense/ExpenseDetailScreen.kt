package com.example.financetracker.feature.expense

import androidx.compose.runtime.Composable
import com.example.financetracker.ui.navigation.PlaceholderScreen

// Dočasná obrazovka. Nahradit obsah funkce skutečnou obrazovkou, parametry ponechat.
// Route: expense/{id}. Smazání přes ConfirmDialog, pak onDeleted().
@Composable
fun ExpenseDetailScreen(
    expenseId: Long,
    onBack: () -> Unit,
    onEdit: (id: Long) -> Unit,
    onDeleted: () -> Unit,
) {
    PlaceholderScreen(
        title = "Detail výdaje #$expenseId",
        onBack = onBack,
        actions = listOf("Upravit" to { onEdit(expenseId) }, "Smazat" to onDeleted),
    )
}
