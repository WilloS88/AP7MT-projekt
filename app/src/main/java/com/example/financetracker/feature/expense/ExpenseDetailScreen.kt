package com.example.financetracker.feature.expense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.automirrored.outlined.Input
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetracker.appContainer
import com.example.financetracker.data.model.Category
import com.example.financetracker.data.model.Expense
import com.example.financetracker.data.repository.BASE_CURRENCY
import com.example.financetracker.ui.components.CategoryBadge
import com.example.financetracker.ui.components.CategoryMiniChip
import com.example.financetracker.ui.components.ConfirmDialog
import com.example.financetracker.ui.components.ErrorState
import com.example.financetracker.ui.components.SourceBadge
import com.example.financetracker.ui.components.VydajnikTopBar
import com.example.financetracker.ui.theme.AppShapes
import com.example.financetracker.ui.theme.Dimens
import com.example.financetracker.ui.theme.Spacing
import com.example.financetracker.ui.theme.tabular
import com.example.financetracker.ui.util.formatAmount
import com.example.financetracker.ui.util.formatCzk
import com.example.financetracker.ui.util.formatDate

// Route: expense/{id}
@Composable
fun ExpenseDetailScreen(
    expenseId: Long,
    onBack: () -> Unit,
    onEdit: (id: Long) -> Unit,
    onDeleted: () -> Unit,
) {
    val viewModel: ExpenseDetailViewModel = viewModel {
        ExpenseDetailViewModel(expenseId, appContainer.expenseRepository, appContainer.categoryRepository)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel.deleted) {
        if (viewModel.deleted) onDeleted()
    }

    Scaffold(topBar = { VydajnikTopBar(title = "Detail výdaje", onBack = onBack) }) { padding ->
        val modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        val current = state
        val expense = current?.expense
        when {
            current == null || viewModel.deleting -> Box(modifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            expense == null -> Box(modifier, contentAlignment = Alignment.Center) {
                ErrorState(
                    title = "Výdaj nenalezen",
                    message = "Výdaj byl mezitím smazán.",
                    onRetry = onBack,
                    retryText = "Zpět",
                )
            }
            else -> DetailContent(
                expense = expense,
                category = current.category,
                onEdit = { onEdit(expense.id) },
                onDelete = { confirmDelete = true },
                modifier = modifier,
            )
        }
    }

    if (confirmDelete) {
        ConfirmDialog(
            title = "Smazat výdaj?",
            text = "Výdaj se trvale odstraní a nepůjde vrátit.",
            confirmText = "Smazat",
            onConfirm = {
                confirmDelete = false
                viewModel.delete()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun DetailContent(
    expense: Expense,
    category: Category?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isForeign = expense.currency != BASE_CURRENCY
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.ScreenMargin, vertical = Spacing.s),
        verticalArrangement = Arrangement.spacedBy(Spacing.xxl),
    ) {
        // Hlavička: kategorie, název, částka
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            if (category != null) {
                CategoryBadge(
                    category = category,
                    size = Dimens.BadgeLarge,
                    iconSize = Dimens.BadgeLargeIcon,
                    shape = AppShapes.Badge56,
                )
            }
            Text(
                text = expense.note ?: category?.name ?: "Výdaj",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Text(formatCzk(expense.amountCzk), style = MaterialTheme.typography.displaySmall.tabular())
            if (isForeign) {
                Text(
                    formatAmount(expense.originalAmount, expense.currency),
                    style = MaterialTheme.typography.bodyMedium.tabular(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Údaje
        Card(
            shape = AppShapes.Card,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        ) {
            Column(Modifier.padding(vertical = Spacing.xxs)) {
                DetailRow(Icons.Outlined.Category, "Kategorie") {
                    if (category != null) CategoryMiniChip(category) else Text("Bez kategorie")
                }
                DetailRow(Icons.Outlined.Event, "Datum") { Text(formatDate(expense.date)) }
                if (isForeign) {
                    DetailRow(Icons.Outlined.CurrencyExchange, "Kurz") {
                        Text("1 ${expense.currency} = ${formatCzk(expense.rate)}")
                    }
                }
                DetailRow(Icons.AutoMirrored.Outlined.Input, "Zdroj") { SourceBadge(expense.source) }
                expense.note?.let { note ->
                    DetailRow(Icons.AutoMirrored.Outlined.Notes, "Poznámka") { Text(note) }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
            FilledTonalButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(Dimens.IconS))
                Text("Upravit", modifier = Modifier.padding(start = Dimens.ButtonIconGap))
            }
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                border = BorderStroke(Dimens.TextFieldBorder, MaterialTheme.colorScheme.error),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(Dimens.IconS))
                Text("Smazat", modifier = Modifier.padding(start = Dimens.ButtonIconGap))
            }
        }
    }
}

/** Řádek karty s údaji: ikona, popisek a hodnota. */
@Composable
private fun DetailRow(icon: ImageVector, label: String, value: @Composable () -> Unit) {
    ListItem(
        leadingContent = { Icon(icon, contentDescription = null) },
        overlineContent = { Text(label) },
        headlineContent = value,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
