package com.example.financetracker

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class VydajnikApplication : Application() {

    lateinit var container: AppContainer
        private set

    /** Scope pro práci, která má přežít obrazovku (např. naplnění výchozích dat). */
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        applicationScope.launch { container.categoryRepository.seedIfEmpty() }
    }
}
