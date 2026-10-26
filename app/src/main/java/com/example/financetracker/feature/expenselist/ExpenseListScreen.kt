package com.example.financetracker.feature.expenselist

import androidx.compose.runtime.Composable
import com.example.financetracker.ui.navigation.PlaceholderScreen

// Dočasná obrazovka. Nahradit obsah funkce skutečnou obrazovkou, parametry ponechat.
// Route: expenses (záložka).
// Kompaktní řádky: CompositionLocalProvider(LocalListDensity provides ListDensity.Compact) { … }
@Composable
fun ExpenseListScreen(
    onAddExpense: () -> Unit,
    onOpenExpense: (id: Long) -> Unit,
) {
    PlaceholderScreen(
        title = "Výdaje",
        onAdd = onAddExpense,
    )
}
