package com.example.financetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Kategorie výdaje.
 *
 * Barva se ukládá jako číslo slotu 1–12, nikdy jako hex (pravidlo z HANDOFF.md).
 * Vykreslení: `VydajnikTheme.categories[category.colorSlot]`.
 * Ikona: `categoryIcon(category.iconKey)` z `ui/components/CategoryIcons.kt`.
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconKey: String,
    val colorSlot: Int,
    val sortOrder: Int = 0,
    /** Výchozí kategorie „Ostatní“ – nejde smazat, přesouvají se do ní výdaje ze smazaných kategorií. */
    val isLocked: Boolean = false,
)

/** Součet výdajů v jedné kategorii – výsledek agregačního dotazu pro přehledy. */
data class CategoryTotal(
    val categoryId: Long,
    val total: Double,
)
