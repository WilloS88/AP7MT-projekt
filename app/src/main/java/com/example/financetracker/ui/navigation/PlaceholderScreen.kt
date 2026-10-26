package com.example.financetracker.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.financetracker.ui.components.AddExpenseFab
import com.example.financetracker.ui.components.VydajnikTopBar
import com.example.financetracker.ui.theme.Spacing

/**
 * Dočasná obrazovka, dokud ji nenahradí skutečná. Ukazuje tlačítka pro všechny
 * navigační akce, aby šla aplikace proklikat.
 * Až budou všechny obrazovky hotové, tento soubor smazat.
 */
@Composable
fun PlaceholderScreen(
    title: String,
    note: String? = null,
    onBack: (() -> Unit)? = null,
    onAdd: (() -> Unit)? = null,
    actions: List<Pair<String, () -> Unit>> = emptyList(),
) {
    Scaffold(
        topBar = { VydajnikTopBar(title = title, onBack = onBack) },
        floatingActionButton = { if (onAdd != null) AddExpenseFab(onClick = onAdd) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Spacing.ScreenMargin),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            Text("Zatím nehotovo", style = MaterialTheme.typography.titleMedium)
            if (note != null) {
                Text(
                    note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            actions.forEach { (label, action) ->
                OutlinedButton(onClick = action) { Text(label) }
            }
        }
    }
}
