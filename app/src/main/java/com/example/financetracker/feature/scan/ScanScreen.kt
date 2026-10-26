package com.example.financetracker.feature.scan

import androidx.compose.runtime.Composable
import com.example.financetracker.data.model.ExpenseSource
import com.example.financetracker.ui.navigation.AddExpensePrefill
import com.example.financetracker.ui.navigation.PlaceholderScreen

// Dočasná obrazovka. Nahradit obsah funkce skutečnou obrazovkou, parametry ponechat.
// Route: scan. Po přečtení kódu zavolat onScanned(prefill) → otevře se předvyplněný formulář výdaje.
@Composable
fun ScanScreen(
    onBack: () -> Unit,
    onScanned: (AddExpensePrefill) -> Unit,
) {
    PlaceholderScreen(
        title = "Skenovat",
        onBack = onBack,
        actions = listOf(
            "Simulovat sken (450 Kč)" to {
                onScanned(
                    AddExpensePrefill(amount = 450.0, currency = "CZK", note = "QR platba", source = ExpenseSource.QR),
                )
            },
        ),
    )
}
