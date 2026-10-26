package com.example.financetracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.financetracker.ui.theme.AppShapes

/**
 * Potvrzovací dialog pro nevratnou akci (smazání výdaje, kategorie…).
 * Potvrzovací tlačítko je plné červené (stock je textové).
 */
@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissText: String = "Zrušit",
    icon: ImageVector = Icons.Outlined.Delete,
) {
    val scheme = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(icon, contentDescription = null, tint = scheme.error) },
        title = { Text(title, style = MaterialTheme.typography.headlineSmall) },
        text = { Text(text, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = { DangerButton(text = confirmText, onClick = onConfirm) },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissText) } },
        containerColor = scheme.surfaceContainerHigh,
        shape = AppShapes.Card,
    )
}

/**
 * Snackbar (např. „Výdaj smazán · Zpět“). Dát do Scaffold(snackbarHost = { AppSnackbarHost(state) }).
 */
@Composable
fun AppSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        Snackbar(
            snackbarData = data,
            shape = AppShapes.Snackbar,
            containerColor = scheme.inverseSurface,
            contentColor = scheme.inverseOnSurface,
            actionColor = scheme.inversePrimary,
        )
    }
}
