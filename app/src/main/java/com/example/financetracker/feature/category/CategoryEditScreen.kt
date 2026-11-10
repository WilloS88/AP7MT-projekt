package com.example.financetracker.feature.category

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetracker.appContainer
import com.example.financetracker.ui.components.AppTextField
import com.example.financetracker.ui.components.CategoryBadge
import com.example.financetracker.ui.components.CategoryChip
import com.example.financetracker.ui.components.CategoryIcons
import com.example.financetracker.ui.components.ConfirmDialog
import com.example.financetracker.ui.components.ErrorState
import com.example.financetracker.ui.components.LargeButton
import com.example.financetracker.ui.components.VydajnikTopBar
import com.example.financetracker.ui.theme.AppShapes
import com.example.financetracker.ui.theme.Dimens
import com.example.financetracker.ui.theme.Spacing
import com.example.financetracker.ui.theme.VydajnikTheme

// Route: categories/edit?id={id}  ([categoryId] null = nová kategorie)
@Composable
fun CategoryEditScreen(
    categoryId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val viewModel: CategoryEditViewModel = viewModel {
        CategoryEditViewModel(categoryId, appContainer.categoryRepository)
    }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel.done) {
        if (viewModel.done) onSaved()
    }

    Scaffold(
        topBar = {
            VydajnikTopBar(
                title = if (viewModel.isEdit) "Upravit kategorii" else "Nová kategorie",
                onBack = onBack,
                actions = {
                    if (viewModel.isEdit && viewModel.loaded && !viewModel.isLocked) {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Smazat kategorii")
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (viewModel.loaded && !viewModel.notFound) {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    LargeButton(
                        text = "Uložit",
                        onClick = viewModel::save,
                        enabled = !viewModel.isSaving,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .imePadding()
                            .padding(horizontal = Spacing.ScreenMargin, vertical = Spacing.m),
                    )
                }
            }
        },
    ) { padding ->
        val modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        when {
            !viewModel.loaded -> Box(modifier, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            viewModel.notFound -> Box(modifier, contentAlignment = Alignment.Center) {
                ErrorState(
                    title = "Kategorie nenalezena",
                    message = "Kategorie byla mezitím smazána.",
                    onRetry = onBack,
                    retryText = "Zpět",
                )
            }
            else -> EditContent(viewModel, modifier)
        }
    }

    if (confirmDelete) {
        ConfirmDialog(
            title = "Smazat kategorii?",
            text = "Výdaje z této kategorie se přesunou do kategorie Ostatní.",
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
private fun EditContent(viewModel: CategoryEditViewModel, modifier: Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.ScreenMargin, vertical = Spacing.s),
        verticalArrangement = Arrangement.spacedBy(Spacing.xxl),
    ) {
        // Živý náhled
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.l),
        ) {
            val preview = viewModel.preview
            CategoryBadge(
                category = preview,
                size = Dimens.BadgeLarge,
                iconSize = Dimens.BadgeLargeIcon,
                shape = AppShapes.Badge56,
            )
            CategoryChip(category = preview, selected = false, onClick = {})
        }

        AppTextField(
            value = viewModel.name,
            onValueChange = viewModel::onNameChange,
            label = "Název",
            isError = viewModel.nameError != null,
            supportingText = viewModel.nameError,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        )

        Section("Barva") {
            ColorPicker(selectedSlot = viewModel.colorSlot, onSelect = viewModel::onColorSelect)
        }
        Section("Ikona") {
            IconPicker(selectedKey = viewModel.iconKey, onSelect = viewModel::onIconSelect)
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        content()
    }
}

/** 12 barev kategorií; vybraná má kroužek a fajfku. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorPicker(selectedSlot: Int, onSelect: (Int) -> Unit) {
    val palette = VydajnikTheme.categories
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
    ) {
        (1..12).forEach { slot ->
            val selected = slot == selectedSlot
            Box(
                modifier = Modifier
                    .size(Dimens.SwatchTarget)
                    .selectable(selected = selected, onClick = { onSelect(slot) }, role = Role.RadioButton),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.size(Dimens.Swatch),
                    shape = CircleShape,
                    color = palette[slot].chart,
                    border = if (selected) BorderStroke(Dimens.SwatchSelectedRing, MaterialTheme.colorScheme.onSurface) else null,
                ) {
                    if (selected) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Vybraná barva",
                                tint = palette[slot].container,
                                modifier = Modifier.size(Dimens.IconM),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Mřížka ikon; vybraná je podbarvená. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IconPicker(selectedKey: String, onSelect: (String) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
    ) {
        CategoryIcons.forEach { (key, icon) ->
            val selected = key == selectedKey
            Surface(
                modifier = Modifier
                    .size(Dimens.IconPickerCell)
                    .selectable(selected = selected, onClick = { onSelect(key) }, role = Role.RadioButton),
                shape = AppShapes.IconBox,
                color = if (selected) scheme.secondaryContainer else Color.Transparent,
                contentColor = if (selected) scheme.onSecondaryContainer else scheme.onSurfaceVariant,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = key)
                }
            }
        }
    }
}
