package com.example.financetracker.feature.dashboard

import androidx.compose.runtime.Composable
import com.example.financetracker.ui.navigation.PlaceholderScreen

// Dočasná obrazovka. Nahradit obsah funkce skutečnou obrazovkou, parametry ponechat.
// Route: dashboard (záložka)
@Composable
fun DashboardScreen(
    onAddExpense: () -> Unit,
    onOpenExpense: (id: Long) -> Unit,
    onScan: () -> Unit,
    onImport: () -> Unit,
) {
    PlaceholderScreen(
        title = "Přehled",
        onAdd = onAddExpense,
        actions = listOf("Skenovat" to onScan, "Importovat" to onImport),
    )
}
