package com.example.financetracker.feature.settings

import androidx.compose.runtime.Composable
import com.example.financetracker.ui.navigation.PlaceholderScreen

// Dočasná obrazovka. Nahradit obsah funkce skutečnou obrazovkou, parametry ponechat.
// Route: settings (záložka)
@Composable
fun SettingsScreen(
    onOpenCategories: () -> Unit,
    onImport: () -> Unit,
) {
    PlaceholderScreen(
        title = "Nastavení",
        actions = listOf("Kategorie" to onOpenCategories, "Importovat výpis" to onImport),
    )
}
