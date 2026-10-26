package com.example.financetracker.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/*
 * Fixed category palette — 12 slots, NEVER derived from dynamic color.
 * Source: p2.css  --c{n} (container) · --c{n}o (on container) · --k{n} (chart).
 *
 * Persist the slot id (1–12) in the database, never a hex value:
 * the color then follows light/dark automatically and survives palette tweaks.
 * A category is always rendered as icon + name + color (never color alone).
 */

@Immutable
data class CategoryColor(
    /** Badge, mini chip and tile background (.cat{n}). */
    val container: Color,
    /** Icon and text on [container]. */
    val onContainer: Color,
    /** Donut segment, bar, legend icon, unselected chip icon (.kb{n} / .kc{n}). */
    val chart: Color,
)

enum class CategorySlot(val id: Int) {
    Orange(1), Blue(2), Teal(3), Purple(4), Pink(5), Neutral(6),
    Red(7), Yellow(8), Green(9), Indigo(10), Brown(11), Mint(12);

    companion object {
        fun fromId(id: Int): CategorySlot = entries.firstOrNull { it.id == id } ?: Neutral
    }
}

@Immutable
data class CategoryPalette(val slots: List<CategoryColor>) {
    init { require(slots.size == 12) { "Category palette must have exactly 12 slots" } }
    operator fun get(slot: CategorySlot): CategoryColor = slots[slot.id - 1]
    operator fun get(slotId: Int): CategoryColor = get(CategorySlot.fromId(slotId))
}

val CategoryPaletteLight = CategoryPalette(
    listOf(
        CategoryColor(Color(0xFFFFDDB3), Color(0xFF2A1800), Color(0xFFC66A00)), //  1 Orange  · Jídlo
        CategoryColor(Color(0xFFD8E2FF), Color(0xFF001A41), Color(0xFF3A5BA9)), //  2 Blue    · Bydlení
        CategoryColor(Color(0xFFBDEBFF), Color(0xFF001F2A), Color(0xFF00829B)), //  3 Teal    · Doprava
        CategoryColor(Color(0xFFF6D9FF), Color(0xFF2E0F3A), Color(0xFF8B4FA8)), //  4 Purple  · Zábava
        CategoryColor(Color(0xFFFFD9E2), Color(0xFF3E001D), Color(0xFFB3365F)), //  5 Pink    · Nákupy
        CategoryColor(Color(0xFFDDE5DB), Color(0xFF151D17), Color(0xFF6B7268)), //  6 Neutral · Ostatní
        CategoryColor(Color(0xFFFFDAD5), Color(0xFF410001), Color(0xFFB4372A)), //  7 Red     · Zdraví
        CategoryColor(Color(0xFFF5E388), Color(0xFF211B00), Color(0xFF8A7300)), //  8 Yellow
        CategoryColor(Color(0xFFC8EFA4), Color(0xFF0C2000), Color(0xFF4C7A1E)), //  9 Green
        CategoryColor(Color(0xFFE1DFFF), Color(0xFF170065), Color(0xFF5B4FCF)), // 10 Indigo
        CategoryColor(Color(0xFFF6DED0), Color(0xFF2B160B), Color(0xFF8A5A3C)), // 11 Brown
        CategoryColor(Color(0xFFB0F1DD), Color(0xFF00201A), Color(0xFF00866E)), // 12 Mint
    )
)

val CategoryPaletteDark = CategoryPalette(
    listOf(
        CategoryColor(Color(0xFF653E00), Color(0xFFFFDDB3), Color(0xFFFFB868)), //  1 Orange
        CategoryColor(Color(0xFF2F4578), Color(0xFFD8E2FF), Color(0xFFADC6FF)), //  2 Blue
        CategoryColor(Color(0xFF004D62), Color(0xFFBDEBFF), Color(0xFF5DD5F2)), //  3 Teal
        CategoryColor(Color(0xFF5C2F6B), Color(0xFFF6D9FF), Color(0xFFE5B4FF)), //  4 Purple
        CategoryColor(Color(0xFF7A2948), Color(0xFFFFD9E2), Color(0xFFFFB0C8)), //  5 Pink
        CategoryColor(Color(0xFF3D4A3F), Color(0xFFDDE5DB), Color(0xFFA9B2A6)), //  6 Neutral
        CategoryColor(Color(0xFF7D2A20), Color(0xFFFFDAD5), Color(0xFFFFB4A8)), //  7 Red
        CategoryColor(Color(0xFF544600), Color(0xFFF5E388), Color(0xFFD8C76F)), //  8 Yellow
        CategoryColor(Color(0xFF2F5112), Color(0xFFC8EFA4), Color(0xFFA6D88A)), //  9 Green
        CategoryColor(Color(0xFF42379F), Color(0xFFE1DFFF), Color(0xFFC2C1FF)), // 10 Indigo
        CategoryColor(Color(0xFF5E4030), Color(0xFFF6DED0), Color(0xFFE2BFA9)), // 11 Brown
        CategoryColor(Color(0xFF005143), Color(0xFFB0F1DD), Color(0xFF66D9BB)), // 12 Mint
    )
)

val LocalCategoryPalette = staticCompositionLocalOf { CategoryPaletteLight }

/** Seed data for first launch (Ostatní is the non-deletable default). */
object DefaultCategories {
    val Jidlo = CategorySlot.Orange
    val Bydleni = CategorySlot.Blue
    val Doprava = CategorySlot.Teal
    val Zabava = CategorySlot.Purple
    val Nakupy = CategorySlot.Pink
    val Ostatni = CategorySlot.Neutral
}
