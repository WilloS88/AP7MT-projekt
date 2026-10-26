package com.example.financetracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.financetracker.data.model.Category
import com.example.financetracker.data.model.Expense

/**
 * Při změně entit zvýšit [version]. Během vývoje se databáze při změně verze smaže
 * (fallbackToDestructiveMigration), takže migrace psát nemusíme.
 */
@Database(
    entities = [Expense::class, Category::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "vydajnik.db")
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
