package com.example.financetracker.feature.importer

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.financetracker.ui.components.ErrorState
import com.example.financetracker.ui.components.VydajnikTopBar
import com.example.financetracker.ui.theme.AppShapes
import com.example.financetracker.ui.theme.Dimens
import com.example.financetracker.ui.theme.Spacing

// Route: import/pick (uvnitř grafu "import").
// [viewModel] sdílí s náhledem navigace (viz importViewModel()).
@Composable
fun ImportPickScreen(
    onBack: () -> Unit,
    onParsed: () -> Unit,
    viewModel: ImportViewModel = importViewModel(),
) {
    val state = viewModel.state

    LaunchedEffect(viewModel.openPreview) {
        if (viewModel.openPreview) {
            viewModel.onPreviewOpened()
            onParsed()
        }
    }

    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.load(uri)
    }

    Scaffold(topBar = { VydajnikTopBar(title = "Import výpisu", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.ScreenMargin, vertical = Spacing.s),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            when (state) {
                is ImportState.Parsing -> ParsingCard(step = state.step)

                is ImportState.Failed -> ErrorState(
                    title = state.title,
                    message = state.message,
                    onRetry = viewModel::reset,
                    retryText = "Vybrat jiný soubor",
                    modifier = Modifier.padding(top = Spacing.xxl),
                )

                else -> {
                    InfoBanner(
                        "Umíme výpisy České spořitelny: export pohybů z George (CSV) nebo měsíční výpis z účtu (PDF). " +
                            "Načtou se jen výdaje, příjmy se přeskočí.",
                    )
                    FileSourceCard(
                        title = "CSV z George",
                        description = "Export pohybů z internetového bankovnictví",
                        icon = Icons.Outlined.TableChart,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        badgeColor = MaterialTheme.colorScheme.primary,
                        badgeContentColor = MaterialTheme.colorScheme.onPrimary,
                        onClick = { pickFile.launch(CsvMimeTypes) },
                    )
                    FileSourceCard(
                        title = "PDF výpis",
                        description = "Měsíční výpis z účtu, textové PDF",
                        icon = Icons.Outlined.PictureAsPdf,
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        badgeColor = MaterialTheme.colorScheme.secondaryContainer,
                        badgeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        onClick = { pickFile.launch(PdfMimeTypes) },
                    )
                }
            }
        }
    }
}

/** Některé správce souborů označují CSV jako text nebo Excel, proto víc typů. */
private val CsvMimeTypes = arrayOf(
    "text/csv", "text/comma-separated-values", "application/csv", "text/plain",
    "application/vnd.ms-excel", "application/octet-stream",
)
private val PdfMimeTypes = arrayOf("application/pdf")

/** Karta pro výběr typu souboru. */
@Composable
private fun FileSourceCard(
    title: String,
    description: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    badgeColor: Color,
    badgeContentColor: Color,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
    ) {
        Row(
            modifier = Modifier.padding(Dimens.CardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.l),
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.Badge)
                    .background(badgeColor, AppShapes.IconBox),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = badgeContentColor, modifier = Modifier.size(Dimens.BadgeIcon))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

/** Informační pruh (tertiaryContainer). */
@Composable
private fun InfoBanner(text: String) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Banner,
        color = scheme.tertiaryContainer,
        contentColor = scheme.onTertiaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(Dimens.BannerPadding),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            Icon(Icons.Outlined.Info, contentDescription = null, modifier = Modifier.size(Dimens.IconM))
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Průběh zpracování: hlavička s ukazatelem a seznam kroků (hotovo / probíhá / čeká). */
@Composable
private fun ParsingCard(step: ParseStep) {
    val scheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.CardSmall,
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainer),
    ) {
        Column(Modifier.padding(Dimens.CardPadding), verticalArrangement = Arrangement.spacedBy(Spacing.l)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.l)) {
                Box(
                    modifier = Modifier
                        .size(Dimens.Badge)
                        .background(scheme.tertiaryContainer, AppShapes.IconBox),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.UploadFile,
                        contentDescription = null,
                        tint = scheme.onTertiaryContainer,
                        modifier = Modifier.size(Dimens.BadgeIcon),
                    )
                }
                Text("Zpracovávám výpis", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            }
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.LinearProgressHeight),
                color = scheme.primary,
                trackColor = scheme.surfaceContainerHighest,
            )
            ParseStep.entries.forEach { entry ->
                ParseStepRow(label = entry.label, done = entry.ordinal < step.ordinal, active = entry == step)
            }
        }
    }
}

@Composable
private fun ParseStepRow(label: String, done: Boolean, active: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.alpha(if (done || active) 1f else PendingAlpha),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.BadgeCompact)
                .background(
                    color = when {
                        done -> scheme.primaryContainer
                        active -> scheme.secondaryContainer
                        else -> scheme.surfaceContainerHighest
                    },
                    shape = AppShapes.Badge,
                ),
            contentAlignment = Alignment.Center,
        ) {
            when {
                done -> Icon(
                    Icons.Outlined.Check,
                    contentDescription = "Hotovo",
                    tint = scheme.onPrimaryContainer,
                    modifier = Modifier.size(Dimens.IconS),
                )
                active -> CircularProgressIndicator(
                    modifier = Modifier.size(Dimens.IconS),
                    color = scheme.onSecondaryContainer,
                )
            }
        }
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

private const val PendingAlpha = 0.6f
