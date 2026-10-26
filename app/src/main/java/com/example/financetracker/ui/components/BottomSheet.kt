package com.example.financetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.financetracker.ui.theme.AppShapes
import com.example.financetracker.ui.theme.Dimens
import com.example.financetracker.ui.theme.Spacing
import com.example.financetracker.ui.theme.VydajnikTheme

/**
 * Spodní panel (výběr kategorie v importu, filtr v seznamu…).
 * Oproti stocku: horní rohy 28 dp, barva surfaceContainerLow, tmavší scrim v tmavém režimu
 * a vlastní úchyt 32 × 4 dp.
 *
 * - [title] != null → nadpis nad obsahem.
 * - Obsah si vodorovné odsazení řeší sám (doporučeno `Spacing.xxl`).
 * - Zavření z kódu: `scope.launch { sheetState.hide() }.invokeOnCompletion { onDismissRequest() }`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = AppShapes.Sheet,
        containerColor = scheme.surfaceContainerLow,
        contentColor = scheme.onSurface,
        scrimColor = VydajnikTheme.scrim,
        dragHandle = { SheetHandle() },
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = scheme.onSurface,
                modifier = Modifier.padding(horizontal = Spacing.xxl, vertical = Spacing.s),
            )
        }
        content()
    }
}

@Composable
private fun SheetHandle() {
    Box(
        modifier = Modifier
            .padding(top = Dimens.SheetHandleMarginTop, bottom = Dimens.SheetHandleMarginBottom)
            .size(width = Dimens.SheetHandleWidth, height = Dimens.SheetHandleHeight)
            .background(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = Dimens.SheetHandleAlpha),
                shape = AppShapes.Pill,
            ),
    )
}
