package com.example.financetracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.financetracker.ui.theme.AppShapes

/**
 * Tlačítko „Přidat výdaj“.
 * [extended] = true → s textem (Dashboard), false → jen ikona (seznamy).
 * Barva primary + tvar 18 dp (stock je primaryContainer + 16 dp).
 */
@Composable
fun AddExpenseFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    extended: Boolean = true,
    text: String = "Přidat výdaj",
) {
    val scheme = MaterialTheme.colorScheme
    if (extended) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            shape = AppShapes.Fab,
            containerColor = scheme.primary,
            contentColor = scheme.onPrimary,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text(text, style = MaterialTheme.typography.titleMedium) },
        )
    } else {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            shape = AppShapes.Fab,
            containerColor = scheme.primary,
            contentColor = scheme.onPrimary,
        ) {
            Icon(Icons.Filled.Add, contentDescription = text)
        }
    }
}
