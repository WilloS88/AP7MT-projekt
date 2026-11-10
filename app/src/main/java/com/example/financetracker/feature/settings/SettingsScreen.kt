package com.example.financetracker.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetracker.appContainer
import com.example.financetracker.data.repository.ExchangeRate
import com.example.financetracker.data.repository.ExchangeRateRepository
import com.example.financetracker.ui.components.RateSourceRow
import com.example.financetracker.ui.components.VydajnikTopBar
import com.example.financetracker.ui.theme.Dimens
import com.example.financetracker.ui.theme.Spacing
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Měna, jejíž kurz Nastavení ukazuje jako ukázku stavu kurzů. */
private const val SampleCurrency = "EUR"

class SettingsViewModel(private val rates: ExchangeRateRepository) : ViewModel() {
    var rate by mutableStateOf<ExchangeRate?>(null)
        private set
    var isRefreshing by mutableStateOf(false)
        private set

    init {
        refresh()
    }

    fun refresh() {
        if (isRefreshing) return
        isRefreshing = true
        viewModelScope.launch {
            rate = try {
                rates.getRateToCzk(SampleCurrency)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            isRefreshing = false
        }
    }
}

// Route: settings (záložka)
@Composable
fun SettingsScreen(
    onOpenCategories: () -> Unit,
    onImport: () -> Unit,
) {
    val viewModel: SettingsViewModel = viewModel { SettingsViewModel(appContainer.exchangeRateRepository) }
    val context = LocalContext.current
    val version = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull()
    }

    Scaffold(topBar = { VydajnikTopBar(title = "Nastavení") }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionHeader("Data")
            SettingsItem(
                icon = Icons.Outlined.Category,
                title = "Kategorie",
                subtitle = "Přidat, upravit nebo smazat kategorie",
                onClick = onOpenCategories,
            )
            SettingsItem(
                icon = Icons.Outlined.UploadFile,
                title = "Importovat výpis",
                subtitle = "CSV nebo PDF z banky",
                onClick = onImport,
            )

            SectionHeader("Kurzy měn")
            RateSourceRow(
                rate = viewModel.rate,
                isRefreshing = viewModel.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.padding(horizontal = Spacing.ScreenMargin),
            )

            SectionHeader("O aplikaci")
            SettingsItem(
                icon = Icons.Outlined.Info,
                title = "Výdajník",
                subtitle = version?.let { "Verze $it" } ?: "Sledování výdajů",
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(Dimens.SectionHeaderPadding),
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
) {
    ListItem(
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
        leadingContent = { Icon(icon, contentDescription = null) },
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = if (onClick != null) {
            { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) }
        } else {
            null
        },
    )
}
