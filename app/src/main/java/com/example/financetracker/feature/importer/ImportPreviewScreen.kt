package com.example.financetracker.feature.importer

import androidx.compose.runtime.Composable
import com.example.financetracker.ui.navigation.PlaceholderScreen

// Dočasná obrazovka. Nahradit obsah funkce skutečnou obrazovkou, parametry ponechat.
// Route: import/preview.
// Kompaktní řádky: CompositionLocalProvider(LocalListDensity provides ListDensity.Compact) { … }
@Composable
fun ImportPreviewScreen(
    onBack: () -> Unit,
    onImported: () -> Unit,
) {
    PlaceholderScreen(
        title = "Náhled importu",
        onBack = onBack,
        actions = listOf("Importovat" to onImported),
    )
}
