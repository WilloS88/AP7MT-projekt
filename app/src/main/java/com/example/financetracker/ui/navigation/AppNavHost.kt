package com.example.financetracker.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.financetracker.feature.category.CategoriesScreen
import com.example.financetracker.feature.category.CategoryEditScreen
import com.example.financetracker.feature.dashboard.DashboardScreen
import com.example.financetracker.feature.expense.AddExpenseScreen
import com.example.financetracker.feature.expense.EditExpenseScreen
import com.example.financetracker.feature.expense.ExpenseDetailScreen
import com.example.financetracker.feature.expenselist.ExpenseListScreen
import com.example.financetracker.feature.importer.ImportPickScreen
import com.example.financetracker.feature.importer.ImportPreviewScreen
import com.example.financetracker.feature.scan.ScanScreen
import com.example.financetracker.feature.settings.SettingsScreen
import com.example.financetracker.feature.stats.StatsScreen

/** Kostra aplikace: spodní lišta (jen na záložkách) + navigace. */
@Composable
fun VydajnikApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = TopLevelDestination.entries.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                VydajnikNavigationBar(currentRoute = currentRoute, onNavigate = navController::navigateToTab)
            }
        },
        // Okraje systému řeší vnitřní Scaffold každé obrazovky
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding),
        )
    }
}

/**
 * Navigační graf. Obrazovky dostávají jen callbacky (kam jít dál), NavController jim
 * nepředáváme – díky tomu se dají obrazovky psát a testovat samostatně.
 */
@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    val back: () -> Unit = { navController.popBackStack() }

    NavHost(navController = navController, startDestination = Routes.DASHBOARD, modifier = modifier) {

        // ---- Záložky
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onAddExpense = { navController.navigate(Routes.add()) },
                onOpenExpense = { navController.navigate(Routes.expenseDetail(it)) },
                onScan = { navController.navigate(Routes.SCAN) },
                onImport = { navController.navigate(Routes.IMPORT_GRAPH) },
            )
        }
        composable(Routes.EXPENSES) {
            ExpenseListScreen(
                onAddExpense = { navController.navigate(Routes.add()) },
                onOpenExpense = { navController.navigate(Routes.expenseDetail(it)) },
            )
        }
        composable(Routes.STATS) {
            StatsScreen()
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onOpenCategories = { navController.navigate(Routes.CATEGORIES) },
                onImport = { navController.navigate(Routes.IMPORT_GRAPH) },
            )
        }

        // ---- Výdaj
        composable(
            route = Routes.ADD,
            arguments = listOf("currency", "amount", "note", "date", "source").map { name ->
                navArgument(name) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            },
        ) { entry ->
            AddExpenseScreen(
                prefill = AddExpensePrefill.fromArguments(entry.arguments),
                onBack = back,
                // Skener nahradí formulář; po skenu se otevře nový, předvyplněný
                onScan = { navController.navigate(Routes.SCAN) { popUpTo(Routes.ADD) { inclusive = true } } },
                onSaved = back,
            )
        }
        composable(
            route = Routes.EXPENSE_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) { entry ->
            ExpenseDetailScreen(
                expenseId = entry.arguments?.getLong("id") ?: 0L,
                onBack = back,
                onEdit = { navController.navigate(Routes.expenseEdit(it)) },
                onDeleted = back,
            )
        }
        composable(
            route = Routes.EXPENSE_EDIT,
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) { entry ->
            EditExpenseScreen(
                expenseId = entry.arguments?.getLong("id") ?: 0L,
                onBack = back,
                onSaved = back,
            )
        }

        // ---- Kategorie
        composable(Routes.CATEGORIES) {
            CategoriesScreen(
                onBack = back,
                onAddCategory = { navController.navigate(Routes.categoryEdit()) },
                onEditCategory = { navController.navigate(Routes.categoryEdit(it)) },
            )
        }
        composable(
            route = Routes.CATEGORY_EDIT,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { entry ->
            CategoryEditScreen(
                categoryId = entry.arguments?.getString("id")?.toLongOrNull(),
                onBack = back,
                onSaved = back,
            )
        }

        // ---- Sken
        composable(Routes.SCAN) {
            ScanScreen(
                onBack = back,
                onScanned = { prefill ->
                    navController.navigate(Routes.add(prefill)) { popUpTo(Routes.SCAN) { inclusive = true } }
                },
            )
        }

        // ---- Import – vnořený graf.
        // Sdílený ViewModel pro pick + preview:
        //   val parent = remember(entry) { navController.getBackStackEntry(Routes.IMPORT_GRAPH) }
        //   val vm: ImportViewModel = viewModel(parent) { ImportViewModel(appContainer.expenseRepository) }
        // a předat `vm` oběma obrazovkám jako parametr.
        navigation(startDestination = Routes.IMPORT_PICK, route = Routes.IMPORT_GRAPH) {
            composable(Routes.IMPORT_PICK) {
                ImportPickScreen(
                    onBack = back,
                    onParsed = { navController.navigate(Routes.IMPORT_PREVIEW) },
                )
            }
            composable(Routes.IMPORT_PREVIEW) {
                ImportPreviewScreen(
                    onBack = back,
                    onImported = { navController.popBackStack(Routes.IMPORT_GRAPH, inclusive = true) },
                )
            }
        }
    }
}

/** Přepnutí záložky: drží jen jednu kopii každé záložky a pamatuje si její stav. */
fun NavHostController.navigateToTab(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
