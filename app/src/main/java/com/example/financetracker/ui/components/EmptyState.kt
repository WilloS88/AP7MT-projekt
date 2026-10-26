package com.example.financetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import com.example.financetracker.ui.theme.AppShapes
import com.example.financetracker.ui.theme.Dimens

/**
 * Prázdný stav – když není co ukázat (první spuštění, žádné výsledky filtru, málo dat).
 * Tlačítko se zobrazí jen se zadaným [actionText] i [onAction].
 */
@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    StateMessage(
        title = title,
        message = message,
        icon = icon,
        badgeColor = scheme.primaryContainer,
        iconColor = scheme.onPrimaryContainer,
        modifier = modifier,
    ) {
        if (actionText != null && onAction != null) {
            Button(onClick = onAction) { Text(actionText) }
        }
    }
}

/** Chybový stav s tlačítkem pro nový pokus (chyba importu, skenu, načítání). */
@Composable
fun ErrorState(
    title: String,
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.ErrorOutline,
    retryText: String = "Zkusit znovu",
) {
    val scheme = MaterialTheme.colorScheme
    StateMessage(
        title = title,
        message = message,
        icon = icon,
        badgeColor = scheme.errorContainer,
        iconColor = scheme.onErrorContainer,
        modifier = modifier,
    ) {
        LargeButton(text = retryText, onClick = onRetry, icon = Icons.Filled.Refresh)
    }
}

@Composable
private fun StateMessage(
    title: String,
    message: String,
    icon: ImageVector,
    badgeColor: Color,
    iconColor: Color,
    modifier: Modifier,
    action: @Composable () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.EmptyStateHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.EmptyStateGap),
    ) {
        Box(
            modifier = Modifier
                .size(width = Dimens.EmptyArtWidth, height = Dimens.EmptyArtHeight)
                .background(scheme.surfaceContainer, AppShapes.Card),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.BadgeLarge)
                    .background(badgeColor, AppShapes.Badge),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(Dimens.BadgeLargeIcon))
            }
        }
        Text(title, style = MaterialTheme.typography.headlineSmall, color = scheme.onSurface, textAlign = TextAlign.Center)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant, textAlign = TextAlign.Center)
        action()
    }
}
