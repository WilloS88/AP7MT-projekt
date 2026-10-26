package com.example.financetracker.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.financetracker.data.model.CategoryTotal
import com.example.financetracker.data.model.Expense
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<Expense>>

    /** Výdaje v rozmezí [from]..[to] včetně, nejnovější první. */
    @Query("SELECT * FROM expenses WHERE date BETWEEN :from AND :to ORDER BY date DESC, id DESC")
    fun observeBetween(from: LocalDate, to: LocalDate): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    fun observeById(id: Long): Flow<Expense?>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): Expense?

    /** Součet v Kč za období (0 když nic). */
    @Query("SELECT COALESCE(SUM(amountCzk), 0) FROM expenses WHERE date BETWEEN :from AND :to")
    fun observeTotalBetween(from: LocalDate, to: LocalDate): Flow<Double>

    /** Součty po kategoriích za období, od největší. */
    @Query(
        """
        SELECT categoryId, SUM(amountCzk) AS total FROM expenses
        WHERE date BETWEEN :from AND :to
        GROUP BY categoryId ORDER BY total DESC
        """
    )
    fun observeCategoryTotals(from: LocalDate, to: LocalDate): Flow<List<CategoryTotal>>

    @Insert
    suspend fun insert(expense: Expense): Long

    @Insert
    suspend fun insertAll(expenses: List<Expense>): List<Long>

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("UPDATE expenses SET categoryId = :toCategoryId WHERE categoryId = :fromCategoryId")
    suspend fun moveToCategory(fromCategoryId: Long, toCategoryId: Long)
}
