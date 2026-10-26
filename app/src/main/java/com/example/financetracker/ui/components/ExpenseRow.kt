package com.example.financetracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import com.example.financetracker.data.model.Category
import com.example.financetracker.data.model.Expense
import com.example.financetracker.data.repository.BASE_CURRENCY
import com.example.financetracker.ui.theme.DefaultCategories
import com.example.financetracker.ui.theme.Spacing
import com.example.financetracker.ui.theme.VydajnikTheme
import com.example.financetracker.ui.theme.tabular
import com.example.financetracker.ui.util.formatAmount
import com.example.financetracker.ui.util.formatCzk
import com.example.financetracker.ui.util.formatDate

/**
 * Řádek výdaje: odznak kategorie, poznámka (nebo název kategorie), pod ní kategorie a datum,
 * vpravo částka v Kč a u cizí měny pod ní původní částka.
 *
 * Velikost se řídí hustotou seznamu – kompaktní řádky (seznam výdajů, náhled importu):
 * `CompositionLocalProvider(LocalListDensity provides ListDensity.Compact) { … }`.
 *
 * - [category] null = kategorie nenalezena → neutrální „Bez kategorie“.
 * - [showDate] false = datum skrýt (např. pod hlavičkou dne).
 * - [enabled] false = ztlumený řádek (např. přeskočený řádek importu).
 * - [leadingContent] = obsah před odznakem, např. zaškrtávátko v náhledu importu.
 */
@Composable
fun ExpenseRow(
    expense: Expense,
    category: Category?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    showDate: Boolean = true,
    enabled: Boolean = true,
    leadingContent: (@Composable () -> Unit)? = null,
) {
    val density = VydajnikTheme.listDensity
    val scheme = MaterialTheme.colorScheme
    val shownCategory = category ?: UnknownCategory
    val note = expense.note?.takeIf { it.isNotBlank() }
    val supporting = listOfNotNull(
        shownCategory.name.takeIf { note != null },
        formatDate(expense.date).takeIf { showDate },
    ).joinToString(" · ")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = density.rowMinHeight)
            .then(if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier)
            .alpha(if (enabled) 1f else DisabledAlpha)
            .padding(
                start = if (leadingContent != null) Spacing.xxs else density.paddingStart,
                end = density.paddingEnd,
                top = Spacing.xs,
                bottom = Spacing.xs,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(density.gap),
    ) {
        leadingContent?.invoke()
        CategoryBadge(category = shownCategory, size = density.badgeSize, iconSize = density.badgeIcon)
        Column(Modifier.weight(1f)) {
            Text(
                text = note ?: shownCategory.name,
                style = density.headline,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (supporting.isNotEmpty()) {
                Text(
                    text = supporting,
                    style = density.supporting,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(formatCzk(expense.amountCzk), style = density.amount, color = scheme.onSurface)
            if (expense.currency != BASE_CURRENCY) {
                Text(
                    text = formatAmount(expense.originalAmount, expense.currency),
                    style = MaterialTheme.typography.bodySmall.tabular(),
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

private const val DisabledAlpha = 0.38f

private val UnknownCategory = Category(
    name = "Bez kategorie",
    iconKey = "other",
    colorSlot = DefaultCategories.Ostatni.id,
)
