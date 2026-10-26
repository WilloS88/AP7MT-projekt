package com.example.financetracker.feature.importer

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetracker.appContainer
import com.example.financetracker.data.model.Category
import com.example.financetracker.data.model.Expense
import com.example.financetracker.data.model.ExpenseSource
import com.example.financetracker.data.repository.BASE_CURRENCY
import com.example.financetracker.data.repository.CategoryRepository
import com.example.financetracker.data.repository.ExchangeRateRepository
import com.example.financetracker.data.repository.ExpenseRepository
import com.example.financetracker.data.repository.convertToCzk
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/** Řádek náhledu importu = budoucí výdaj + jestli se má uložit. */
data class ImportRow(
    val id: Int,
    val expense: Expense,
    val selected: Boolean,
    val rawLine: String,
)

/** Kroky načítání souboru (zobrazují se v průběhu). */
enum class ParseStep(val label: String) {
    Reading("Načítám soubor"),
    Converting("Přepočítávám cizí měny"),
}

sealed interface ImportState {
    data object Idle : ImportState
    data class Parsing(val step: ParseStep) : ImportState
    data class Preview(
        val fileName: String,
        val format: StatementFormat,
        val rows: List<ImportRow>,
        val isSaving: Boolean = false,
    ) : ImportState {
        val selectedRows: List<ImportRow> get() = rows.filter { it.selected }
        val selectedTotalCzk: Double get() = selectedRows.sumOf { it.expense.amountCzk }
    }
    data class Failed(val title: String, val message: String) : ImportState
    data class Done(val count: Int, val totalCzk: Double) : ImportState
}

/**
 * Sdílený stav importu pro výběr souboru i náhled. Musí být navázaný na graf "import",
 * aby obě obrazovky viděly stejná data – viz [importViewModel].
 */
class ImportViewModel(
    private val expenseRepository: ExpenseRepository,
    categoryRepository: CategoryRepository,
    private val exchangeRateRepository: ExchangeRateRepository,
    private val reader: StatementReader,
) : ViewModel() {

    var state by mutableStateOf<ImportState>(ImportState.Idle)
        private set

    /** true hned po načtení souboru → výběr souboru přejde do náhledu (jen jednou). */
    var openPreview by mutableStateOf(false)
        private set

    val categories: StateFlow<List<Category>> = categoryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Nové řádky dostanou kategorii „Ostatní“, uživatel ji v náhledu změní. */
    private var defaultCategoryId: Long? = null

    init {
        viewModelScope.launch { defaultCategoryId = categoryRepository.getDefault()?.id }
    }

    fun load(uri: Uri) {
        state = ImportState.Parsing(ParseStep.Reading)
        viewModelScope.launch {
            state = try {
                val statement = reader.read(uri)
                state = ImportState.Parsing(ParseStep.Converting)
                val rates = loadRates(statement.transactions)
                val categoryId = defaultCategoryId ?: categories.first { it.isNotEmpty() }.first().id
                val rows = statement.transactions.mapIndexed { index, transaction ->
                    ImportRow(
                        id = index,
                        expense = transaction.toExpense(categoryId, rates),
                        selected = true,
                        rawLine = transaction.rawLine,
                    )
                }
                openPreview = true
                ImportState.Preview(statement.fileName, statement.format, rows)
            } catch (e: CancellationException) {
                throw e
            } catch (e: StatementException) {
                ImportState.Failed(e.title, e.message)
            } catch (e: Exception) {
                ImportState.Failed("Výpis se nepodařilo načíst", e.message ?: "Neznámá chyba při čtení souboru.")
            }
        }
    }

    fun onPreviewOpened() {
        openPreview = false
    }

    fun toggle(rowId: Int) = updateRows { row -> if (row.id == rowId) row.copy(selected = !row.selected) else row }

    fun setAllSelected(selected: Boolean) = updateRows { it.copy(selected = selected) }

    fun setCategory(rowId: Int, categoryId: Long) = updateRows { row ->
        if (row.id == rowId) row.withCategory(categoryId) else row
    }

    fun setCategoryForSelected(categoryId: Long) = updateRows { row ->
        if (row.selected) row.withCategory(categoryId) else row
    }

    fun import() {
        val preview = state as? ImportState.Preview ?: return
        val expenses = preview.selectedRows.map { it.expense }
        if (expenses.isEmpty() || preview.isSaving) return
        state = preview.copy(isSaving = true)
        viewModelScope.launch {
            state = try {
                expenseRepository.addAll(expenses)
                ImportState.Done(count = expenses.size, totalCzk = expenses.sumOf { it.amountCzk })
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ImportState.Failed("Výdaje se nepodařilo uložit", e.message ?: "Zkuste import zopakovat.")
            }
        }
    }

    /** Zpět na výběr souboru (po chybě nebo po dokončení). */
    fun reset() {
        state = ImportState.Idle
        openPreview = false
    }

    private fun updateRows(transform: (ImportRow) -> ImportRow) {
        val preview = state as? ImportState.Preview ?: return
        state = preview.copy(rows = preview.rows.map(transform))
    }

    /** Kurz pro měny, u kterých výpis neuvádí částku v Kč. */
    private suspend fun loadRates(transactions: List<StatementTransaction>): Map<String, Double> =
        transactions
            .filter { it.amountCzk == null && it.currency != BASE_CURRENCY }
            .map { it.currency }
            .distinct()
            .associateWith { currency ->
                try {
                    exchangeRateRepository.getRateToCzk(currency).rateToCzk
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    throw StatementException(
                        "Nepodařilo se načíst kurz $currency",
                        "Výpis obsahuje platby v $currency. Připojte se k internetu a zkuste to znovu.",
                    )
                }
            }

    private fun StatementTransaction.toExpense(categoryId: Long, rates: Map<String, Double>): Expense {
        val czk = amountCzk ?: if (currency == BASE_CURRENCY) amount else convertToCzk(amount, rates.getValue(currency))
        return Expense(
            amountCzk = czk,
            originalAmount = amount,
            currency = currency,
            rate = if (currency == BASE_CURRENCY) 1.0 else rates[currency] ?: (czk / amount),
            categoryId = categoryId,
            date = date,
            note = note,
            source = ExpenseSource.IMPORT,
        )
    }

    private fun ImportRow.withCategory(categoryId: Long) = copy(expense = expense.copy(categoryId = categoryId))
}

/**
 * ViewModel importu navázaný na [owner]. Navigace předává oběma obrazovkám stejný,
 * navázaný na záznam grafu "import":
 * ```
 * val parent = remember(entry) { navController.getBackStackEntry(Routes.IMPORT_GRAPH) }
 * ImportPickScreen(…, viewModel = importViewModel(parent))
 * ```
 */
@Composable
fun importViewModel(
    owner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current),
): ImportViewModel = viewModel(viewModelStoreOwner = owner) {
    val application = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
    ImportViewModel(
        expenseRepository = appContainer.expenseRepository,
        categoryRepository = appContainer.categoryRepository,
        exchangeRateRepository = appContainer.exchangeRateRepository,
        reader = StatementReader(application),
    )
}
