package com.example.financetracker

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.financetracker.data.db.AppDatabase
import com.example.financetracker.data.repository.CategoryRepository
import com.example.financetracker.data.repository.ExchangeRateRepository
import com.example.financetracker.data.repository.ExpenseRepository
import com.example.financetracker.data.repository.FakeExchangeRateRepository

/**
 * Jednoduché ruční DI – jedna instance na celou aplikaci (vytváří ji [VydajnikApplication]).
 *
 * Použití ve ViewModelu:
 * ```
 * val vm: DashboardViewModel = viewModel { DashboardViewModel(appContainer.expenseRepository) }
 * ```
 */
class AppContainer(context: Context) {
    val database: AppDatabase = AppDatabase.create(context)

    val expenseRepository = ExpenseRepository(database.expenseDao())
    val categoryRepository = CategoryRepository(database)

    // TODO: nahradit skutečnou implementací přes REST API
    val exchangeRateRepository: ExchangeRateRepository = FakeExchangeRateRepository()
}

/** Zkratka pro `viewModel { … }` – zpřístupní [AppContainer] uvnitř inicializátoru. */
val CreationExtras.appContainer: AppContainer
    get() = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as VydajnikApplication).container
