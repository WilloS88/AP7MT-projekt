package com.example.financetracker.data.repository

import androidx.room.withTransaction
import com.example.financetracker.data.db.AppDatabase
import com.example.financetracker.data.model.Category
import com.example.financetracker.ui.theme.DefaultCategories
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val db: AppDatabase) {

    private val dao = db.categoryDao()

    fun observeAll(): Flow<List<Category>> = dao.observeAll()

    suspend fun get(id: Long): Category? = dao.getById(id)

    /** Výchozí kategorie „Ostatní“. */
    suspend fun getDefault(): Category? = dao.getLocked()

    /** Nová kategorie se zařadí na konec seznamu. Vrací id. */
    suspend fun add(category: Category): Long =
        dao.insert(category.copy(sortOrder = dao.maxSortOrder() + 1))

    suspend fun update(category: Category) = dao.update(category)

    /** Smaže kategorii a její výdaje přesune do „Ostatní“. Zamčenou kategorii smazat nejde. */
    suspend fun delete(category: Category) {
        if (category.isLocked) return
        db.withTransaction {
            val fallback = dao.getLocked() ?: return@withTransaction
            db.expenseDao().moveToCategory(category.id, fallback.id)
            dao.delete(category)
        }
    }

    /** Při prvním spuštění naplní výchozí kategorie. */
    suspend fun seedIfEmpty() {
        if (dao.count() > 0) return
        dao.insertAll(
            listOf(
                Category(name = "Jídlo", iconKey = "food", colorSlot = DefaultCategories.Jidlo.id, sortOrder = 1),
                Category(name = "Bydlení", iconKey = "home", colorSlot = DefaultCategories.Bydleni.id, sortOrder = 2),
                Category(name = "Doprava", iconKey = "transport", colorSlot = DefaultCategories.Doprava.id, sortOrder = 3),
                Category(name = "Zábava", iconKey = "fun", colorSlot = DefaultCategories.Zabava.id, sortOrder = 4),
                Category(name = "Nákupy", iconKey = "shopping", colorSlot = DefaultCategories.Nakupy.id, sortOrder = 5),
                Category(name = "Zdraví", iconKey = "health", colorSlot = 7, sortOrder = 6),
                Category(name = "Ostatní", iconKey = "other", colorSlot = DefaultCategories.Ostatni.id, sortOrder = 100, isLocked = true),
            )
        )
    }
}
