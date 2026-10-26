package com.example.financetracker.data.repository

import com.example.financetracker.data.db.ExpenseDao
import com.example.financetracker.data.model.CategoryTotal
import com.example.financetracker.data.model.Expense
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** Jediný vstup k výdajům pro všechny obrazovky (A, B i C). */
class ExpenseRepository(private val dao: ExpenseDao) {

    fun observeAll(): Flow<List<Expense>> = dao.observeAll()

    fun observeBetween(from: LocalDate, to: LocalDate): Flow<List<Expense>> = dao.observeBetween(from, to)

    fun observe(id: Long): Flow<Expense?> = dao.observeById(id)

    suspend fun get(id: Long): Expense? = dao.getById(id)

    fun observeTotalBetween(from: LocalDate, to: LocalDate): Flow<Double> = dao.observeTotalBetween(from, to)

    fun observeCategoryTotals(from: LocalDate, to: LocalDate): Flow<List<CategoryTotal>> =
        dao.observeCategoryTotals(from, to)

    /** Vrací id nového výdaje. */
    suspend fun add(expense: Expense): Long = dao.insert(expense)

    /** Pro import. */
    suspend fun addAll(expenses: List<Expense>): List<Long> = dao.insertAll(expenses)

    suspend fun update(expense: Expense) = dao.update(expense)

    suspend fun delete(expense: Expense) = dao.delete(expense)
}
