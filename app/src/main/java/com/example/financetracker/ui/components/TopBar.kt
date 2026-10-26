package com.example.financetracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.financetracker.ui.theme.Dimens

/**
 * Horní lišta.
 *
 * - [onBack] != null → šipka zpět vlevo.
 * - [overline] != null → dvouřádkový titulek jako na Dashboardu („Výdajník“ + měsíc).
 * - [actions] → ikony vpravo (např. sken, import).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VydajnikTopBar(
    title: String,
    modifier: Modifier = Modifier,
    overline: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    TopAppBar(
        modifier = modifier,
        title = {
            // Bez šipky zpět je titulek odsazený 20 dp (stock 16) → +4 dp
            val titleModifier = if (onBack == null) {
                Modifier.padding(start = Dimens.TopBarTitleInset - 16.dp)
            } else {
                Modifier
            }
            if (overline != null) {
                Column(titleModifier) {
                    Text(overline, style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                    Text(title, style = MaterialTheme.typography.titleLarge)
                }
            } else {
                Text(title, modifier = titleModifier, style = MaterialTheme.typography.titleLarge)
            }
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zpět")
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = scheme.surface,
            scrolledContainerColor = scheme.surface,
            titleContentColor = scheme.onSurface,
            navigationIconContentColor = scheme.onSurface,
            actionIconContentColor = scheme.onSurfaceVariant,
        ),
    )
}
