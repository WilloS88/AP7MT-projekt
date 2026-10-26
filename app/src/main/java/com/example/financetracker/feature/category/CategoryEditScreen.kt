package com.example.financetracker.feature.category

import androidx.compose.runtime.Composable
import com.example.financetracker.ui.navigation.PlaceholderScreen

// Dočasná obrazovka. Nahradit obsah funkce skutečnou obrazovkou, parametry ponechat.
// Route: categories/edit?id={id}  ([categoryId] null = nová kategorie)
@Composable
fun CategoryEditScreen(
    categoryId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    PlaceholderScreen(
        title = if (categoryId == null) "Nová kategorie" else "Upravit kategorii",
        onBack = onBack,
        actions = listOf("Uložit" to onSaved),
    )
}
