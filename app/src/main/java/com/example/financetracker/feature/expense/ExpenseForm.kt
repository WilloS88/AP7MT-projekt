package com.example.financetracker.feature.expense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetracker.appContainer
import com.example.financetracker.data.model.ExpenseSource
import com.example.financetracker.ui.components.CategoryPicker
import com.example.financetracker.ui.components.CurrencyAmountInput
import com.example.financetracker.ui.components.DateField
import com.example.financetracker.ui.components.ErrorState
import com.example.financetracker.ui.components.LargeButton
import com.example.financetracker.ui.components.NoteField
import com.example.financetracker.ui.components.SourceBadge
import com.example.financetracker.ui.components.VydajnikTopBar
import com.example.financetracker.ui.navigation.AddExpensePrefill
import com.example.financetracker.ui.theme.Dimens
import com.example.financetracker.ui.theme.Spacing
import com.example.financetracker.ui.util.formatCzk

/** ViewModel formuláře; [editId] null = nový výdaj. */
@Composable
fun expenseFormViewModel(editId: Long?, prefill: AddExpensePrefill = AddExpensePrefill()): ExpenseFormViewModel =
    viewModel {
        ExpenseFormViewModel(
            editId = editId,
            prefill = prefill,
            expenseRepository = appContainer.expenseRepository,
            categoryRepository = appContainer.categoryRepository,
        )
    }

/**
 * Formulář výdaje – sdílí ho přidání i úprava.
 * [onScan] != null → v horní liště tlačítko skenu (jen u nového výdaje).
 */
@Composable
fun ExpenseForm(
    title: String,
    viewModel: ExpenseFormViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onScan: (() -> Unit)? = null,
) {
    LaunchedEffect(viewModel.saved) {
        if (viewModel.saved) onSaved()
    }

    Scaffold(
        topBar = {
            VydajnikTopBar(
                title = title,
                onBack = onBack,
                actions = {
                    if (onScan != null) {
                        IconButton(onClick = onScan) {
                            Icon(Icons.Outlined.QrCodeScanner, contentDescription = "Skenovat QR kód")
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (viewModel.load == FormLoad.Ready) {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    LargeButton(
                        text = if (viewModel.isEdit) "Uložit změny" else "Uložit výdaj",
                        onClick = viewModel::save,
                        enabled = viewModel.canSave,
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
        when (viewModel.load) {
            FormLoad.Loading -> Box(modifier, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            FormLoad.NotFound -> Box(modifier, contentAlignment = Alignment.Center) {
                ErrorState(
                    title = "Výdaj nenalezen",
                    message = "Výdaj byl mezitím smazán.",
                    onRetry = onBack,
                    retryText = "Zpět",
                )
            }
            FormLoad.Ready -> FormContent(viewModel, modifier)
        }
    }
}

@Composable
private fun FormContent(viewModel: ExpenseFormViewModel, modifier: Modifier) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.ScreenMargin, vertical = Spacing.s),
        verticalArrangement = Arrangement.spacedBy(Spacing.l),
    ) {
        if (viewModel.source != ExpenseSource.MANUAL) {
            SourceBadge(viewModel.source)
        }

        CurrencyAmountInput(
            amount = viewModel.amountText,
            currency = viewModel.currency,
            onAmountChange = viewModel::onAmountChange,
            onCurrencyChange = viewModel::onCurrencyChange,
            onRateChange = viewModel::onRateChange,
            isError = viewModel.amountError != null,
            errorText = viewModel.amountError,
        )

        val originalRate = viewModel.originalRate
        if (originalRate != null) {
            RateChoice(
                currency = viewModel.currency,
                originalRate = originalRate,
                todayRate = viewModel.liveRate?.takeIf { it.currency == viewModel.currency }?.rateToCzk,
                keepOriginal = viewModel.keepOriginalRate,
                onKeepOriginalChange = viewModel::onKeepOriginalRateChange,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Text("Kategorie", style = MaterialTheme.typography.titleSmall)
            CategoryPicker(
                categories = categories,
                selectedId = viewModel.categoryId,
                onSelect = viewModel::onCategorySelect,
                isError = viewModel.categoryError != null,
            )
            viewModel.categoryError?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }

        DateField(date = viewModel.date, onDateChange = viewModel::onDateChange)
        NoteField(value = viewModel.note, onValueChange = viewModel::onNoteChange)
    }
}

/** Při úpravě v cizí měně: ponechat kurz z data výdaje, nebo přepočítat dnešním. */
@Composable
private fun RateChoice(
    currency: String,
    originalRate: Double,
    todayRate: Double?,
    keepOriginal: Boolean,
    onKeepOriginalChange: (Boolean) -> Unit,
) {
    Column(Modifier.selectableGroup()) {
        Text("Přepočet na Kč", style = MaterialTheme.typography.titleSmall)
        RateOption(
            text = "Ke dni výdaje · 1 $currency = ${formatCzk(originalRate)}",
            selected = keepOriginal,
            onClick = { onKeepOriginalChange(true) },
        )
        RateOption(
            text = "Dnešní kurz · " + (todayRate?.let { "1 $currency = ${formatCzk(it)}" } ?: "načítá se…"),
            selected = !keepOriginal,
            onClick = { onKeepOriginalChange(false) },
        )
    }
}

@Composable
private fun RateOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.MinTouchTarget)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}
