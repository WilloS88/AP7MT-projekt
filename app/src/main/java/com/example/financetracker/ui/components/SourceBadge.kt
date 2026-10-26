package com.example.financetracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.example.financetracker.data.model.ExpenseSource
import com.example.financetracker.ui.theme.AppShapes
import com.example.financetracker.ui.theme.Dimens
import com.example.financetracker.ui.theme.Spacing

/**
 * Odznak „odkud výdaj přišel“ (detail výdaje, výsledek skenu).
 * Sken a import jsou zvýrazněné (tertiaryContainer), ručně zadaný výdaj je neutrální.
 */
@Composable
fun SourceBadge(source: ExpenseSource, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val isManual = source == ExpenseSource.MANUAL
    Surface(
        modifier = modifier.height(Dimens.SourceBadgeHeight),
        shape = AppShapes.Pill,
        color = if (isManual) scheme.surfaceContainerHigh else scheme.tertiaryContainer,
        contentColor = if (isManual) scheme.onSurfaceVariant else scheme.onTertiaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(start = Dimens.SourceBadgePaddingStart, end = Dimens.SourceBadgePaddingEnd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
        ) {
            Icon(source.icon, contentDescription = null, modifier = Modifier.size(Dimens.IconXxs))
            Text(source.label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Český popisek zdroje, např. pro čtečku obrazovky nebo text v detailu. */
val ExpenseSource.label: String
    get() = when (this) {
        ExpenseSource.QR -> "QR kód"
        ExpenseSource.IMPORT -> "Import výpisu"
        ExpenseSource.MANUAL -> "Zadáno ručně"
    }

private val ExpenseSource.icon: ImageVector
    get() = when (this) {
        ExpenseSource.QR -> Icons.Outlined.QrCodeScanner
        ExpenseSource.IMPORT -> Icons.Outlined.UploadFile
        ExpenseSource.MANUAL -> Icons.Outlined.Edit
    }
