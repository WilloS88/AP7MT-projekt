package com.example.financetracker.feature.expense

import androidx.compose.runtime.Composable
import com.example.financetracker.ui.navigation.AddExpensePrefill
import com.example.financetracker.ui.navigation.PlaceholderScreen

// Dočasná obrazovka. Nahradit obsah funkce skutečnou obrazovkou, parametry ponechat.
// Route: add?currency=&amount=&note=&date=&source=
// [prefill] = předvyplněné hodnoty (např. ze skenu QR Platby).
@Composable
fun AddExpenseScreen(
    prefill: AddExpensePrefill,
    onBack: () -> Unit,
    onScan: () -> Unit,
    onSaved: () -> Unit,
) {
    PlaceholderScreen(
        title = "Nový výdaj",
        note = "Předvyplněno: $prefill",
        onBack = onBack,
        actions = listOf("Skenovat" to onScan, "Uložit" to onSaved),
    )
}
