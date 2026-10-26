package com.example.financetracker.feature.importer

import androidx.compose.runtime.Composable
import com.example.financetracker.ui.navigation.PlaceholderScreen

// Dočasná obrazovka. Nahradit obsah funkce skutečnou obrazovkou, parametry ponechat.
// Route: import/pick (uvnitř grafu "import").
// Načtená data předat do náhledu sdíleným ViewModelem navázaným na graf "import"
// (viz poznámka u importGraph v AppNavHost.kt).
@Composable
fun ImportPickScreen(
    onBack: () -> Unit,
    onParsed: () -> Unit,
) {
    PlaceholderScreen(
        title = "Import výpisu",
        onBack = onBack,
        actions = listOf("Simulovat načtení souboru" to onParsed),
    )
}
