package com.example.financetracker.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/** Odkud výdaj přišel – zobrazuje se jako odznak zdroje. */
enum class ExpenseSource { MANUAL, QR, IMPORT }

/**
 * Jeden výdaj.
 *
 * Kurz se ukládá ke každému výdaji (pravidlo z HANDOFF.md), takže pozdější změna kurzu
 * nezmění už uložené částky. Pro CZK je [rate] = 1.0 a [originalAmount] == [amountCzk].
 */
@Entity(
    tableName = "expenses",
    indices = [Index("categoryId"), Index("date")],
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Částka přepočtená na Kč – s ní počítají všechny přehledy. */
    val amountCzk: Double,
    /** Částka v původní měně. */
    val originalAmount: Double,
    /** ISO kód měny, např. "CZK", "EUR". */
    val currency: String = "CZK",
    /** Kolik Kč stojí 1 jednotka [currency] v okamžiku uložení. */
    val rate: Double = 1.0,
    val categoryId: Long,
    val date: LocalDate,
    val note: String? = null,
    val source: ExpenseSource = ExpenseSource.MANUAL,
)
