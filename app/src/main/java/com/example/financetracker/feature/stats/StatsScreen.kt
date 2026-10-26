package com.example.financetracker.feature.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetracker.appContainer
import com.example.financetracker.ui.components.EmptyState
import com.example.financetracker.ui.components.VydajnikTopBar
import com.example.financetracker.ui.theme.AppShapes
import com.example.financetracker.ui.theme.Dimens
import com.example.financetracker.ui.theme.Spacing

// Route: stats (záložka)
@Composable
fun StatsScreen() {
    val viewModel: StatsViewModel = viewModel {
        StatsViewModel(appContainer.expenseRepository, appContainer.categoryRepository)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(topBar = { VydajnikTopBar(title = "Statistiky") }) { padding ->
        val current = state ?: return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.ScreenMargin),
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
        ) {
            PeriodSwitcher(
                period = current.period,
                onTypeChange = viewModel::selectType,
                onPrevious = viewModel::previous,
                onNext = viewModel::next,
            )
            if (current.spending.isEmpty()) {
                EmptyState(
                    title = "Žádné výdaje",
                    message = "V tomto období zatím nemáte žádné výdaje.",
                    icon = Icons.Outlined.PieChart,
                    modifier = Modifier.padding(top = Spacing.xxl),
                )
            } else {
                Card(
                    shape = AppShapes.Card,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    modifier = Modifier.padding(bottom = Spacing.l),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.HeroPaddingHorizontal, vertical = Dimens.HeroPaddingVertical),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.xl),
                    ) {
                        Text(
                            text = "Útrata podle kategorií",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        CategoryDonut(spending = current.spending, total = current.total)
                        CategoryLegend(spending = current.spending, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

/** Volba měsíc / rok a listování mezi obdobími. */
@Composable
private fun PeriodSwitcher(
    period: Period,
    onTypeChange: (PeriodType) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.SegmentedHeight),
        ) {
            PeriodType.entries.forEachIndexed { index, type ->
                SegmentedButton(
                    selected = period.type == type,
                    onClick = { onTypeChange(type) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = PeriodType.entries.size),
                    label = { Text(type.label) },
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Předchozí období")
            }
            Text(
                text = period.label,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onNext, enabled = period.canGoNext) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Další období")
            }
        }
    }
}
