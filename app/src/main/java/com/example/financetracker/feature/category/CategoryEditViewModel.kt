package com.example.financetracker.feature.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.VydajnikApplication
import com.example.financetracker.data.model.Category
import com.example.financetracker.data.repository.CategoryRepository
import com.example.financetracker.ui.components.CategoryIcons
import com.example.financetracker.ui.theme.CategorySlot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Seznam kategorií z databáze (pro obrazovku Kategorie). */
@Composable
fun categoryList(): Flow<List<Category>> {
    val context = LocalContext.current
    return remember { (context.applicationContext as VydajnikApplication).container.categoryRepository.observeAll() }
}

/**
 * Vytvoření ([categoryId] null) nebo úprava kategorie.
 * Barva se ukládá jako číslo slotu 1–12, ne jako hex.
 */
class CategoryEditViewModel(
    private val categoryId: Long?,
    private val repository: CategoryRepository,
) : ViewModel() {

    val isEdit: Boolean get() = categoryId != null

    var loaded by mutableStateOf(false)
        private set
    var notFound by mutableStateOf(false)
        private set

    var name by mutableStateOf("")
        private set
    var iconKey by mutableStateOf(CategoryIcons.keys.first())
        private set
    var colorSlot by mutableStateOf(CategorySlot.Orange.id)
        private set
    var isLocked by mutableStateOf(false)
        private set

    var showErrors by mutableStateOf(false)
        private set
    var isSaving by mutableStateOf(false)
        private set
    /** true = uloženo nebo smazáno → obrazovka se zavře. */
    var done by mutableStateOf(false)
        private set

    private var original: Category? = null
    private var otherNames: Set<String> = emptySet()

    init {
        viewModelScope.launch {
            val all = repository.observeAll().first()
            original = categoryId?.let { id -> all.firstOrNull { it.id == id } }
            otherNames = all.filter { it.id != categoryId }.map { it.name.trim().lowercase() }.toSet()

            val current = original
            when {
                categoryId != null && current == null -> notFound = true
                current != null -> {
                    name = current.name
                    iconKey = current.iconKey
                    colorSlot = current.colorSlot
                    isLocked = current.isLocked
                }
                else -> {
                    // Nová kategorie: první ikona a barva, které zatím nikdo nepoužívá
                    iconKey = CategoryIcons.keys.firstOrNull { key -> all.none { it.iconKey == key } } ?: iconKey
                    colorSlot = (1..12).firstOrNull { slot -> all.none { it.colorSlot == slot } } ?: colorSlot
                }
            }
            loaded = true
        }
    }

    val nameError: String?
        get() = when {
            !showErrors -> null
            name.isBlank() -> "Zadejte název"
            name.trim().lowercase() in otherNames -> "Kategorie s tímto názvem už existuje"
            else -> null
        }

    /** Náhled kategorie podle aktuálně vybraných hodnot. */
    val preview: Category
        get() = Category(
            id = categoryId ?: 0,
            name = name.trim().ifEmpty { "Nová kategorie" },
            iconKey = iconKey,
            colorSlot = colorSlot,
            isLocked = isLocked,
        )

    fun onNameChange(value: String) { name = value }
    fun onIconSelect(key: String) { iconKey = key }
    fun onColorSelect(slot: Int) { colorSlot = slot }

    fun save() {
        showErrors = true
        if (nameError != null || isSaving) return
        isSaving = true
        viewModelScope.launch {
            val base = original
            if (base == null) {
                repository.add(preview.copy(id = 0))
            } else {
                repository.update(base.copy(name = name.trim(), iconKey = iconKey, colorSlot = colorSlot))
            }
            done = true
        }
    }

    /** Smaže kategorii, její výdaje se přesunou do „Ostatní“. */
    fun delete() {
        val base = original ?: return
        if (base.isLocked || isSaving) return
        isSaving = true
        viewModelScope.launch {
            repository.delete(base)
            done = true
        }
    }
}
