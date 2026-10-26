package com.example.financetracker.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetracker.appContainer
import com.example.financetracker.feature.stats.CategorySpending
import com.example.financetracker.ui.components.AddExpenseFab
import com.example.financetracker.ui.components.CategoryBadge
import com.example.financetracker.ui.components.EmptyState
import com.example.financetracker.ui.components.ExpenseRow
import com.example.financetracker.ui.components.VydajnikTopBar
import com.example.financetracker.ui.theme.AppShapes
import com.example.financetracker.ui.theme.AppTextStyles
import com.example.financetracker.ui.theme.Dimens
import com.example.financetracker.ui.theme.Spacing
import com.example.financetracker.ui.theme.VydajnikTheme
import com.example.financetracker.ui.theme.tabular
import com.example.financetracker.ui.util.formatCzk
import com.example.financetracker.ui.util.formatMonth

// Route: dashboard (záložka)
@Composable
fun DashboardScreen(
    onAddExpense: () -> Unit,
    onOpenExpense: (id: Long) -> Unit,
    onScan: () -> Unit,
    onImport: () -> Unit,
) {
    val viewModel: DashboardViewModel = viewModel {
        DashboardViewModel(appContainer.expenseRepository, appContainer.categoryRepository)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            VydajnikTopBar(
                title = state?.month?.let(::formatMonth).orEmpty(),
                overline = "Výdajník",
                actions = {
                    IconButton(onClick = onScan) {
                        Icon(Icons.Outlined.QrCodeScanner, contentDescription = "Skenovat QR kód")
                    }
                    IconButton(onClick = onImport) {
                        Icon(Icons.Outlined.UploadFile, contentDescription = "Importovat výpis")
                    }
                },
            )
        },
        floatingActionButton = { AddExpenseFab(onClick = onAddExpense) },
    ) { padding ->
        val current = state ?: return@Scaffold
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                // Místo pod posledním řádkem, aby ho nepřekrylo tlačítko Přidat
                bottom = padding.calculateBottomPadding() + Dimens.FabHeight + Spacing.l * 2,
            ),
        ) {
            item {
                MonthTotalCard(
                    total = current.monthTotal,
                    modifier = Modifier.padding(horizontal = Spacing.ScreenMargin, vertical = Spacing.s),
                )
            }
            if (current.spending.isNotEmpty()) {
                item { SectionHeader("Podle kategorií") }
                item { CategorySpendingRow(current.spending) }
            }
            if (current.recent.isNotEmpty()) {
                item { SectionHeader("Poslední výdaje") }
                items(current.recent, key = { it.id }) { expense ->
                    ExpenseRow(
                        expense = expense,
                        category = current.categories[expense.categoryId],
                        onClick = { onOpenExpense(expense.id) },
                    )
                }
            } else {
                item {
                    EmptyState(
                        title = "Zatím žádné výdaje",
                        message = "Přidejte první výdaj, naskenujte QR platbu nebo importujte výpis z banky.",
                        actionText = "Naskenovat QR platbu",
                        onAction = onScan,
                        modifier = Modifier.padding(top = Spacing.xxl),
                    )
                }
            }
        }
    }
}

/** Kolik je utraceno tento měsíc. Bez výdajů neutrální barva. */
@Composable
private fun MonthTotalCard(total: Double, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val isEmpty = total == 0.0
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = if (isEmpty) scheme.surfaceContainer else scheme.primaryContainer,
            contentColor = if (isEmpty) scheme.onSurfaceVariant else scheme.onPrimaryContainer,
        ),
    ) {
        Column(Modifier.padding(horizontal = Dimens.HeroPaddingHorizontal, vertical = Dimens.HeroPaddingVertical)) {
            Text("Utraceno tento měsíc", style = MaterialTheme.typography.bodyMedium)
            Text(formatCzk(total), style = MaterialTheme.typography.displaySmall.tabular())
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.MinTouchTarget)
            .padding(start = Spacing.xl, end = Spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

/** Dlaždice s útratou v jednotlivých kategoriích, od největší. */
@Composable
private fun CategorySpendingRow(spending: List<CategorySpending>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Spacing.ScreenMargin),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        items(spending, key = { it.category.id }) { item -> CategoryTile(item) }
    }
}

@Composable
private fun CategoryTile(item: CategorySpending) {
    val scheme = MaterialTheme.colorScheme
    val colors = VydajnikTheme.categories[item.category.colorSlot]
    Card(
        modifier = Modifier.width(Dimens.CategoryTileWidth),
        shape = AppShapes.CardSmall,
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainer),
    ) {
        Column(Modifier.padding(Dimens.CategoryTilePadding)) {
            CategoryBadge(category = item.category, shape = AppShapes.IconBox)
            Spacer(Modifier.height(Spacing.s))
            Text(
                text = item.category.name,
                style = AppTextStyles.Caption13,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            BasicText(
                text = formatCzk(item.total),
                style = MaterialTheme.typography.titleMedium.tabular().copy(color = scheme.onSurface),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = MaterialTheme.typography.labelSmall.fontSize,
                    maxFontSize = MaterialTheme.typography.titleMedium.fontSize,
                ),
            )
            Spacer(Modifier.height(Spacing.s))
            // Podíl na útratě měsíce
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(Dimens.LinearProgressHeight)
                    .background(scheme.surfaceContainerHighest, AppShapes.Pill),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(item.share)
                        .height(Dimens.LinearProgressHeight)
                        .background(colors.chart, AppShapes.Pill),
                )
            }
        }
    }
}
