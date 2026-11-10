package com.example.financetracker.feature.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financetracker.data.model.Category
import com.example.financetracker.ui.components.AddExpenseFab
import com.example.financetracker.ui.components.CategoryBadge
import com.example.financetracker.ui.components.VydajnikTopBar
import com.example.financetracker.ui.theme.Dimens

// Route: categories
@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    onAddCategory: () -> Unit,
    onEditCategory: (id: Long) -> Unit,
) {
    val categories by categoryList().collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = { VydajnikTopBar(title = "Kategorie", onBack = onBack) },
        floatingActionButton = { AddExpenseFab(onClick = onAddCategory, text = "Nová kategorie") },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            // Místo pod posledním řádkem, aby ho nepřekrylo plovoucí tlačítko
            contentPadding = PaddingValues(bottom = Dimens.FabHeight + Dimens.FabMarginBottomAboveNav * 2),
        ) {
            items(categories, key = { it.id }) { category ->
                CategoryRow(category = category, onClick = { onEditCategory(category.id) })
            }
        }
    }
}

@Composable
private fun CategoryRow(category: Category, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = { CategoryBadge(category) },
        headlineContent = { Text(category.name) },
        supportingContent = if (category.isLocked) {
            { Text("Výchozí kategorie, nejde smazat") }
        } else {
            null
        },
        trailingContent = {
            Icon(
                imageVector = if (category.isLocked) Icons.Outlined.Lock else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}
