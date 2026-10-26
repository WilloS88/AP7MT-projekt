package com.example.financetracker.feature.category

import androidx.compose.runtime.Composable
import com.example.financetracker.ui.navigation.PlaceholderScreen

// Dočasná obrazovka. Nahradit obsah funkce skutečnou obrazovkou, parametry ponechat.
// Route: categories
@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    onAddCategory: () -> Unit,
    onEditCategory: (id: Long) -> Unit,
) {
    PlaceholderScreen(
        title = "Kategorie",
        onBack = onBack,
        actions = listOf("Nová kategorie" to onAddCategory),
    )
}
