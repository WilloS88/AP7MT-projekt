package com.example.financetracker.feature.importer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financetracker.data.model.Category
import com.example.financetracker.ui.components.AppBottomSheet
import com.example.financetracker.ui.components.CategoryPicker
import com.example.financetracker.ui.components.EmptyState
import com.example.financetracker.ui.components.ErrorState
import com.example.financetracker.ui.components.ExpenseRow
import com.example.financetracker.ui.components.LargeButton
import com.example.financetracker.ui.components.VydajnikTopBar
import com.example.financetracker.ui.theme.AppShapes
import com.example.financetracker.ui.theme.Dimens
import com.example.financetracker.ui.theme.ListDensity
import com.example.financetracker.ui.theme.LocalListDensity
import com.example.financetracker.ui.theme.Spacing
import com.example.financetracker.ui.theme.tabular
import com.example.financetracker.ui.util.formatCzk
import kotlinx.coroutines.launch

// Route: import/preview.
// [viewModel] sdílí s výběrem souboru navigace (viz importViewModel()).
@Composable
fun ImportPreviewScreen(
    onBack: () -> Unit,
    onImported: () -> Unit,
    viewModel: ImportViewModel = importViewModel(),
) {
    val state = viewModel.state
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var sheetTarget by remember { mutableStateOf<CategoryTarget?>(null) }

    val finish = {
        viewModel.reset()
        onImported()
    }
    // Po uložení už se nemá jít zpět do náhledu → zpět = hotovo
    BackHandler(enabled = state is ImportState.Done, onBack = finish)

    Scaffold(
        topBar = {
            VydajnikTopBar(
                title = if (state is ImportState.Done) "Import dokončen" else "Náhled importu",
                onBack = if (state is ImportState.Done) null else onBack,
            )
        },
        bottomBar = {
            if (state is ImportState.Preview) {
                ConfirmBar(state = state, onConfirm = viewModel::import)
            }
        },
    ) { padding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(padding)
        when (state) {
            is ImportState.Preview -> PreviewList(
                state = state,
                categories = categories,
                onToggle = viewModel::toggle,
                onSelectAll = viewModel::setAllSelected,
                onEditCategory = { sheetTarget = it },
                modifier = contentModifier,
            )

            is ImportState.Done -> Box(contentModifier, contentAlignment = Alignment.Center) {
                ImportDone(state = state, onDone = finish)
            }

            is ImportState.Failed -> Box(contentModifier, contentAlignment = Alignment.Center) {
                ErrorState(
                    title = state.title,
                    message = state.message,
                    onRetry = {
                        viewModel.reset()
                        onBack()
                    },
                    retryText = "Vybrat jiný soubor",
                )
            }

            else -> Box(contentModifier, contentAlignment = Alignment.Center) {
                EmptyState(
                    title = "Nejdřív vyberte soubor",
                    message = "Náhled se ukáže po načtení výpisu z banky.",
                    actionText = "Vybrat soubor",
                    onAction = onBack,
                )
            }
        }
    }

    val preview = state as? ImportState.Preview
    val target = sheetTarget
    if (preview != null && target != null) {
        CategorySheet(
            target = target,
            preview = preview,
            categories = categories,
            onSelect = { categoryId ->
                when (target) {
                    CategoryTarget.Selected -> viewModel.setCategoryForSelected(categoryId)
                    is CategoryTarget.Row -> viewModel.setCategory(target.rowId, categoryId)
                }
            },
            onDismiss = { sheetTarget = null },
        )
    }
}

/** Komu se mění kategorie: všem vybraným řádkům, nebo jednomu. */
private sealed interface CategoryTarget {
    data object Selected : CategoryTarget
    data class Row(val rowId: Int) : CategoryTarget
}

@Composable
private fun PreviewList(
    state: ImportState.Preview,
    categories: List<Category>,
    onToggle: (Int) -> Unit,
    onSelectAll: (Boolean) -> Unit,
    onEditCategory: (CategoryTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoriesById = remember(categories) { categories.associateBy { it.id } }
    val selectedCount = state.selectedRows.size

    CompositionLocalProvider(LocalListDensity provides ListDensity.Compact) {
        LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = Spacing.l)) {
            item {
                FileHeader(
                    state = state,
                    modifier = Modifier.padding(horizontal = Spacing.ScreenMargin, vertical = Spacing.s),
                )
            }
            item {
                SelectAllRow(
                    selectedCount = selectedCount,
                    totalCount = state.rows.size,
                    onSelectAll = onSelectAll,
                )
            }
            if (selectedCount > 0) {
                item {
                    BulkCategoryBar(
                        selectedCount = selectedCount,
                        onChangeCategory = { onEditCategory(CategoryTarget.Selected) },
                        modifier = Modifier.padding(horizontal = Spacing.ScreenMargin, vertical = Spacing.xxs),
                    )
                }
            }
            items(state.rows, key = { it.id }) { row ->
                ExpenseRow(
                    expense = row.expense,
                    category = categoriesById[row.expense.categoryId],
                    onClick = { onEditCategory(CategoryTarget.Row(row.id)) },
                    enabled = row.selected,
                    leadingContent = {
                        Checkbox(checked = row.selected, onCheckedChange = { onToggle(row.id) })
                    },
                )
            }
        }
    }
}

/** Načtený soubor: název, počet výdajů a součet vybraných. */
@Composable
private fun FileHeader(state: ImportState.Preview, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.CardSmall,
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier.padding(Dimens.CardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.l),
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.Badge)
                    .background(scheme.tertiaryContainer, AppShapes.IconBox),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (state.format == StatementFormat.PDF) Icons.Outlined.PictureAsPdf else Icons.Outlined.TableChart,
                    contentDescription = null,
                    tint = scheme.onTertiaryContainer,
                    modifier = Modifier.size(Dimens.BadgeIcon),
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    state.fileName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${expensesText(state.rows.size)} · vybráno ${formatCzk(state.selectedTotalCzk)}",
                    style = MaterialTheme.typography.bodyMedium.tabular(),
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SelectAllRow(selectedCount: Int, totalCount: Int, onSelectAll: (Boolean) -> Unit) {
    val toggleState = when (selectedCount) {
        0 -> ToggleableState.Off
        totalCount -> ToggleableState.On
        else -> ToggleableState.Indeterminate
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Spacing.xxs, end = Spacing.l),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TriStateCheckbox(state = toggleState, onClick = { onSelectAll(selectedCount < totalCount) })
        Text("Vybrat vše", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(
            "$selectedCount z $totalCount",
            style = MaterialTheme.typography.bodyMedium.tabular(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Pruh s akcí „změnit kategorii“ pro všechny vybrané řádky. */
@Composable
private fun BulkCategoryBar(selectedCount: Int, onChangeCategory: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Banner,
        color = scheme.secondaryContainer,
        contentColor = scheme.onSecondaryContainer,
    ) {
        Row(
            modifier = Modifier
                .height(Dimens.MinTouchTarget)
                .padding(start = Spacing.l, end = Spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Vybráno: $selectedCount", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            Button(onClick = onChangeCategory) { Text("Kategorie") }
        }
    }
}

/** Spodní lišta s potvrzením importu. */
@Composable
private fun ConfirmBar(state: ImportState.Preview, onConfirm: () -> Unit) {
    val count = state.selectedRows.size
    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Box(
            Modifier
                .navigationBarsPadding()
                .padding(start = Spacing.l, top = Spacing.m, end = Spacing.l, bottom = Spacing.xl),
        ) {
            LargeButton(
                text = when {
                    state.isSaving -> "Ukládám…"
                    count == 0 -> "Vyberte výdaje"
                    else -> "Importovat ${expensesText(count)}"
                },
                onClick = onConfirm,
                enabled = count > 0 && !state.isSaving,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySheet(
    target: CategoryTarget,
    preview: ImportState.Preview,
    categories: List<Category>,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val rows = when (target) {
        CategoryTarget.Selected -> preview.selectedRows
        is CategoryTarget.Row -> preview.rows.filter { it.id == target.rowId }
    }
    // Mají-li všechny řádky stejnou kategorii, je ve výběru označená
    val current = rows.map { it.expense.categoryId }.distinct().singleOrNull()

    AppBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        title = when (target) {
            CategoryTarget.Selected -> "Kategorie pro ${expensesText(rows.size)}"
            is CategoryTarget.Row -> "Kategorie výdaje"
        },
    ) {
        Column(
            modifier = Modifier.padding(start = Spacing.xxl, end = Spacing.xxl, bottom = Spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
        ) {
            (target as? CategoryTarget.Row)?.let {
                rows.firstOrNull()?.let { row ->
                    Text(
                        "${row.expense.note.orEmpty()} · ${formatCzk(row.expense.amountCzk)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            CategoryPicker(
                categories = categories,
                selectedId = current,
                onSelect = { category ->
                    onSelect(category.id)
                    scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                },
            )
        }
    }
}

/** Shrnutí po uložení. */
@Composable
private fun ImportDone(state: ImportState.Done, onDone: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.EmptyStateHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.EmptyStateGap),
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.BadgeLarge)
                .background(scheme.primaryContainer, AppShapes.Badge56),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Check,
                contentDescription = null,
                tint = scheme.onPrimaryContainer,
                modifier = Modifier.size(Dimens.BadgeLargeIcon),
            )
        }
        Text("Uloženo ${expensesText(state.count)}", style = MaterialTheme.typography.headlineSmall)
        Text(formatCzk(state.totalCzk), style = MaterialTheme.typography.displaySmall.tabular(), color = scheme.primary)
        Text(
            "Najdete je v přehledu i v seznamu výdajů.",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
        )
        LargeButton(text = "Hotovo", onClick = onDone)
    }
}

/** 1 výdaj, 2 výdaje, 5 výdajů. */
private fun expensesText(count: Int): String = when {
    count == 1 -> "1 výdaj"
    count in 2..4 -> "$count výdaje"
    else -> "$count výdajů"
}
