package com.example.financetracker.feature.expenselist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetracker.appContainer
import com.example.financetracker.data.model.Category
import com.example.financetracker.ui.components.AddExpenseFab
import com.example.financetracker.ui.components.CategoryChip
import com.example.financetracker.ui.components.EmptyState
import com.example.financetracker.ui.components.ExpenseRow
import com.example.financetracker.ui.components.VydajnikTopBar
import com.example.financetracker.ui.theme.Dimens
import com.example.financetracker.ui.theme.ListDensity
import com.example.financetracker.ui.theme.LocalListDensity
import com.example.financetracker.ui.theme.Spacing
import com.example.financetracker.ui.theme.tabular
import com.example.financetracker.ui.util.CzechLocale
import com.example.financetracker.ui.util.formatCzk
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Route: expenses (záložka)
@Composable
fun ExpenseListScreen(
    onAddExpense: () -> Unit,
    onOpenExpense: (id: Long) -> Unit,
) {
    val viewModel: ExpenseListViewModel = viewModel {
        ExpenseListViewModel(appContainer.expenseRepository, appContainer.categoryRepository)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { VydajnikTopBar(title = "Výdaje") },
        floatingActionButton = { AddExpenseFab(onClick = onAddExpense, extended = false) },
    ) { padding ->
        val current = state ?: return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (current.allCount == 0) {
                EmptyState(
                    title = "Zatím žádné výdaje",
                    message = "Výdaje, které přidáte, se objeví tady.",
                    modifier = Modifier.padding(top = Spacing.xxl),
                )
            } else {
                SearchField(
                    query = viewModel.query,
                    onQueryChange = viewModel::onQueryChange,
                    modifier = Modifier.padding(horizontal = Spacing.ScreenMargin),
                )
                CategoryFilterRow(
                    categories = current.categories,
                    selectedIds = current.selectedCategoryIds,
                    onToggle = viewModel::toggleCategory,
                )
                if (current.days.isEmpty()) {
                    EmptyState(
                        title = "Nic jsme nenašli",
                        message = "Zkuste hledat jinak nebo zrušte filtr kategorií.",
                        icon = Icons.Outlined.SearchOff,
                        actionText = "Zrušit filtry",
                        onAction = viewModel::clearFilters,
                        modifier = Modifier.padding(top = Spacing.xxl),
                    )
                } else {
                    ExpenseDayList(
                        days = current.days,
                        categories = current.categoriesById,
                        onOpenExpense = onOpenExpense,
                    )
                }
            }
        }
    }
}

/** Filtr podle kategorií – lze vybrat víc najednou, nic vybráno = všechny. */
@Composable
private fun CategoryFilterRow(
    categories: List<Category>,
    selectedIds: Set<Long>,
    onToggle: (Long) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Spacing.ScreenMargin, vertical = Spacing.s),
        horizontalArrangement = Arrangement.spacedBy(Dimens.ChipRowGap),
    ) {
        items(categories, key = { it.id }) { category ->
            CategoryChip(
                category = category,
                selected = category.id in selectedIds,
                onClick = { onToggle(category.id) },
            )
        }
    }
}

/** Výdaje seskupené po dnech, hlavička dne zůstává nahoře při posunu. Kompaktní řádky. */
@Composable
private fun ExpenseDayList(
    days: List<DayGroup>,
    categories: Map<Long, Category>,
    onOpenExpense: (id: Long) -> Unit,
) {
    val today = remember { LocalDate.now() }
    CompositionLocalProvider(LocalListDensity provides ListDensity.Compact) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            // Místo pod posledním řádkem, aby ho nepřekrylo tlačítko Přidat
            contentPadding = PaddingValues(bottom = Dimens.FabHeight + Spacing.l * 2),
        ) {
            days.forEach { day ->
                stickyHeader(key = "day-${day.date}") {
                    DayHeader(label = dayLabel(day.date, today), total = day.total)
                }
                items(day.expenses, key = { it.id }) { expense ->
                    ExpenseRow(
                        expense = expense,
                        category = categories[expense.categoryId],
                        onClick = { onOpenExpense(expense.id) },
                        showDate = false,
                    )
                }
            }
        }
    }
}

@Composable
private fun DayHeader(label: String, total: Double) {
    val scheme = MaterialTheme.colorScheme
    val style = MaterialTheme.typography.labelMedium
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.DayHeaderHeight)
            .background(scheme.surface)
            .padding(horizontal = Spacing.ScreenMargin),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label.uppercase(CzechLocale),
            style = style,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(formatCzk(total), style = style.tabular(), fontWeight = FontWeight.SemiBold, color = scheme.onSurfaceVariant)
    }
}

private val weekdayFormatter = DateTimeFormatter.ofPattern("EEEE d. M.", CzechLocale)
private val weekdayYearFormatter = DateTimeFormatter.ofPattern("EEEE d. M. yyyy", CzechLocale)

/** "Dnes", "Včera", "pondělí 19. 10.", u jiného roku i s rokem. */
private fun dayLabel(date: LocalDate, today: LocalDate): String = when (date) {
    today -> "Dnes"
    today.minusDays(1) -> "Včera"
    else -> date.format(if (date.year == today.year) weekdayFormatter else weekdayYearFormatter)
}
