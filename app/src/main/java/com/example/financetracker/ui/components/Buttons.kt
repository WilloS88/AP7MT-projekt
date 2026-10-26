package com.example.financetracker.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.financetracker.ui.theme.AppShapes
import com.example.financetracker.ui.theme.Dimens

/*
 * Tlačítka: pro běžné použití stačí stock Button / FilledTonalButton / OutlinedButton /
 * TextButton (výška 40 dp a barvy už sedí díky theme). Tady jsou jen varianty, které se od stocku liší.
 */

/** Velké tlačítko přes celou šířku (Uložit, Potvrdit import…). Výška 56 dp. */
@Composable
fun LargeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.ButtonLargeHeight),
        shape = AppShapes.Pill,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(Dimens.IconS))
            Spacer(Modifier.width(Dimens.ButtonIconGap))
        }
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

/** Červené tlačítko pro nevratné akce (Smazat). */
@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
        ),
    ) {
        Text(text)
    }
}
