package com.example.financetracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.example.financetracker.data.model.Category
import com.example.financetracker.ui.theme.AppShapes
import com.example.financetracker.ui.theme.Dimens
import com.example.financetracker.ui.theme.Spacing
import com.example.financetracker.ui.theme.VydajnikTheme

/**
 * Kulatý (nebo čtvercový) odznak kategorie s ikonou – v řádku výdaje, detailu výdaje atd.
 * Barva podle slotu kategorie, nikdy z MaterialTheme.colorScheme.
 */
@Composable
fun CategoryBadge(
    category: Category,
    modifier: Modifier = Modifier,
    size: Dp = Dimens.Badge,
    iconSize: Dp = Dimens.BadgeIcon,
    shape: Shape = CircleShape,
) {
    val colors = VydajnikTheme.categories[category.colorSlot]
    Surface(modifier = modifier.size(size), shape = shape, color = colors.container) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = categoryIcon(category.iconKey),
                contentDescription = null,
                tint = colors.onContainer,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

/**
 * Chip kategorie (výběr ve formuláři, filtr v seznamu).
 * Výška 36 dp, nevybraný má ikonu v barvě kategorie, vybraný má místo ikony fajfku.
 */
@Composable
fun CategoryChip(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
) {
    val cat = VydajnikTheme.categories[category.colorSlot]
    val scheme = MaterialTheme.colorScheme
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(Dimens.ChipHeight),
        shape = AppShapes.Chip,
        label = { Text(category.name) },
        leadingIcon = {
            Icon(
                imageVector = if (selected) Icons.Filled.Check else categoryIcon(category.iconKey),
                contentDescription = null,
                tint = if (selected) scheme.onSecondaryContainer else cat.chart,
                modifier = Modifier.size(Dimens.ChipIcon),
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            labelColor = if (isError) scheme.error else scheme.onSurfaceVariant,
            selectedContainerColor = scheme.secondaryContainer,
            selectedLabelColor = scheme.onSecondaryContainer,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = selected,
            borderColor = if (isError) scheme.error else scheme.outlineVariant,
            borderWidth = Dimens.ChipBorder,
        ),
    )
}

/** Mini varianta chipu kategorie – jen pro zobrazení (řádek importu, detail). Výška 24 dp. */
@Composable
fun CategoryMiniChip(category: Category, modifier: Modifier = Modifier) {
    val cat = VydajnikTheme.categories[category.colorSlot]
    Surface(modifier = modifier.height(Dimens.MiniChipHeight), shape = AppShapes.Mini, color = cat.container) {
        Row(
            modifier = Modifier.padding(start = Dimens.MiniChipPaddingStart, end = Dimens.MiniChipPaddingEnd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
        ) {
            Icon(
                imageVector = categoryIcon(category.iconKey),
                contentDescription = null,
                tint = cat.onContainer,
                modifier = Modifier.size(Dimens.IconXxs),
            )
            Text(category.name, style = MaterialTheme.typography.labelMedium, color = cat.onContainer)
        }
    }
}

/**
 * Výběr jedné kategorie – chipy zalomené do řádků (formulář výdaje, výsledek skenu, import).
 * [selectedId] null = nic nevybráno.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryPicker(
    categories: List<Category>,
    selectedId: Long?,
    onSelect: (Category) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dimens.ChipRowGap),
        verticalArrangement = Arrangement.spacedBy(Dimens.ChipRowGap),
    ) {
        categories.forEach { category ->
            CategoryChip(
                category = category,
                selected = category.id == selectedId,
                onClick = { onSelect(category) },
                isError = isError,
            )
        }
    }
}
