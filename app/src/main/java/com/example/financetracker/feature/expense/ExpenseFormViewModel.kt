package com.example.financetracker.feature.expense

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.model.Category
import com.example.financetracker.data.model.Expense
import com.example.financetracker.data.model.ExpenseSource
import com.example.financetracker.data.repository.BASE_CURRENCY
import com.example.financetracker.data.repository.CategoryRepository
import com.example.financetracker.data.repository.ExchangeRate
import com.example.financetracker.data.repository.ExpenseRepository
import com.example.financetracker.data.repository.convertToCzk
import com.example.financetracker.ui.navigation.AddExpensePrefill
import com.example.financetracker.ui.util.parseAmount
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate

/** Stav načítání výdaje (jen při úpravě). */
enum class FormLoad { Loading, Ready, NotFound }

/**
 * Sdílený formulář pro nový výdaj i úpravu.
 *
 * - [editId] null = nový výdaj (předvyplněný z [prefill]), jinak úprava existujícího.
 * - Kurz posílá komponenta zadání částky přes [onRateChange]. Při úpravě se stejnou měnou
 *   lze ponechat původní kurz uložený u výdaje ([keepOriginalRate]).
 */
class ExpenseFormViewModel(
    private val editId: Long?,
    prefill: AddExpensePrefill,
    private val expenseRepository: ExpenseRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {

    val isEdit: Boolean get() = editId != null

    val categories: StateFlow<List<Category>> = categoryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var load by mutableStateOf(if (editId == null) FormLoad.Ready else FormLoad.Loading)
        private set

    var amountText by mutableStateOf(prefill.amount?.let(::amountToText).orEmpty())
        private set
    var currency by mutableStateOf(prefill.currency ?: BASE_CURRENCY)
        private set
    var categoryId by mutableStateOf<Long?>(null)
        private set
    var date by mutableStateOf(prefill.date ?: LocalDate.now())
        private set
    var note by mutableStateOf(prefill.note.orEmpty())
        private set

    /** Kurz od komponenty zadání částky (dnešní). null = ještě se načítá. */
    var liveRate by mutableStateOf<ExchangeRate?>(null)
        private set

    /** Při úpravě: ponechat kurz uložený u výdaje (platí jen když se nezměnila měna). */
    var keepOriginalRate by mutableStateOf(true)
        private set

    /** Chyby ukazujeme až po prvním pokusu o uložení. */
    var showErrors by mutableStateOf(false)
        private set
    var isSaving by mutableStateOf(false)
        private set
    var saved by mutableStateOf(false)
        private set

    /** Odkud výdaj přišel – u úpravy se zachová. */
    var source by mutableStateOf(prefill.source ?: ExpenseSource.MANUAL)
        private set
    private var original: Expense? = null

    init {
        if (editId != null) {
            viewModelScope.launch {
                val expense = expenseRepository.get(editId)
                if (expense == null) {
                    load = FormLoad.NotFound
                } else {
                    original = expense
                    amountText = amountToText(expense.originalAmount)
                    currency = expense.currency
                    categoryId = expense.categoryId
                    date = expense.date
                    note = expense.note.orEmpty()
                    source = expense.source
                    load = FormLoad.Ready
                }
            }
        }
    }

    // ---- Odvozené hodnoty

    val amount: Double? get() = parseAmount(amountText)?.takeIf { it > 0 }

    /** Původní kurz výdaje, pokud jde o úpravu v nezměněné cizí měně. */
    val originalRate: Double?
        get() = original?.takeIf { it.currency == currency && it.currency != BASE_CURRENCY }?.rate

    /** Kurz, se kterým se výdaj uloží. */
    val effectiveRate: Double?
        get() = originalRate?.takeIf { keepOriginalRate } ?: liveRate?.takeIf { it.currency == currency }?.rateToCzk

    val amountError: String?
        get() = when {
            !showErrors -> null
            amountText.isBlank() -> "Zadejte částku"
            amount == null -> "Částka musí být větší než 0"
            else -> null
        }

    val categoryError: String?
        get() = if (showErrors && categoryId == null) "Vyberte kategorii" else null

    /** Uložit jde, až je kurz – bez něj nejde spočítat částka v Kč. */
    val canSave: Boolean get() = !isSaving && effectiveRate != null && load == FormLoad.Ready

    // ---- Události z UI

    fun onAmountChange(value: String) { amountText = value }
    fun onCurrencyChange(value: String) { currency = value }
    fun onRateChange(rate: ExchangeRate?) { liveRate = rate }
    fun onCategorySelect(category: Category) { categoryId = category.id }
    fun onDateChange(value: LocalDate) { date = value }
    fun onNoteChange(value: String) { note = value }
    fun onKeepOriginalRateChange(value: Boolean) { keepOriginalRate = value }

    fun save() {
        showErrors = true
        val amount = amount ?: return
        val categoryId = categoryId ?: return
        val rate = effectiveRate ?: return
        if (isSaving) return

        isSaving = true
        val expense = Expense(
            id = original?.id ?: 0,
            amountCzk = convertToCzk(amount, rate),
            originalAmount = amount,
            currency = currency,
            rate = rate,
            categoryId = categoryId,
            date = date,
            note = note.trim().ifEmpty { null },
            source = source,
        )
        viewModelScope.launch {
            if (original == null) expenseRepository.add(expense) else expenseRepository.update(expense)
            isSaving = false
            saved = true
        }
    }
}

/** 450.0 → "450", 12.5 → "12,5" (formát, který očekává pole pro částku). */
internal fun amountToText(amount: Double): String =
    BigDecimal.valueOf(amount).stripTrailingZeros().toPlainString().replace('.', ',')
