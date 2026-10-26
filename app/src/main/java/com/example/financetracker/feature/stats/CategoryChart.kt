package com.example.financetracker.feature.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.financetracker.ui.components.categoryIcon
import com.example.financetracker.ui.theme.AppTextStyles
import com.example.financetracker.ui.theme.Dimens
import com.example.financetracker.ui.theme.Spacing
import com.example.financetracker.ui.theme.VydajnikTheme
import com.example.financetracker.ui.theme.tabular
import com.example.financetracker.ui.util.CzechLocale
import com.example.financetracker.ui.util.formatCzk
import java.text.NumberFormat

/** Koláčový (prstencový) graf útraty podle kategorií, uprostřed celková částka. */
@Composable
fun CategoryDonut(
    spending: List<CategorySpending>,
    total: Double,
    modifier: Modifier = Modifier,
) {
    val palette = VydajnikTheme.categories
    val colors = spending.map { palette[it.category.colorSlot].chart }
    val description = spending.joinToString(", ") { "${it.category.name} ${formatPercent(it.share)}" }
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .size(Dimens.DonutSize)
            .semantics { contentDescription = "Útrata podle kategorií: $description" },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(Dimens.DonutSize)) {
            val stroke = Dimens.DonutStroke.toPx()
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val gap = if (spending.size > 1) Dimens.DonutGapDegrees else 0f
            var start = -90f
            spending.forEachIndexed { index, item ->
                val sweep = item.share * 360f
                drawArc(
                    color = colors[index],
                    startAngle = start + gap / 2,
                    sweepAngle = (sweep - gap).coerceAtLeast(0f),
                    useCenter = false,
                    topLeft = Offset(stroke / 2, stroke / 2),
                    size = arcSize,
                    style = Stroke(width = stroke),
                )
                start += sweep
            }
        }
        Column(
            modifier = Modifier.width(Dimens.DonutSize - (Dimens.DonutStroke + Spacing.s) * 2),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Celkem", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
            BasicText(
                text = formatCzk(total),
                style = MaterialTheme.typography.titleLarge.tabular().copy(color = scheme.onSurface),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = MaterialTheme.typography.labelSmall.fontSize,
                    maxFontSize = MaterialTheme.typography.titleLarge.fontSize,
                ),
            )
        }
    }
}

/** Legenda grafu: ikona v barvě kategorie, název, podíl a částka. */
@Composable
fun CategoryLegend(
    spending: List<CategorySpending>,
    modifier: Modifier = Modifier,
) {
    val palette = VydajnikTheme.categories
    val scheme = MaterialTheme.colorScheme
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        spending.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = categoryIcon(item.category.iconKey),
                    contentDescription = null,
                    tint = palette[item.category.colorSlot].chart,
                    modifier = Modifier.size(Dimens.IconXs),
                )
                Spacer(Modifier.width(Spacing.s))
                Text(
                    text = item.category.name,
                    style = AppTextStyles.Caption13,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(formatPercent(item.share), style = AppTextStyles.Caption13.tabular(), color = scheme.onSurfaceVariant)
                Spacer(Modifier.width(Spacing.m))
                Text(
                    text = formatCzk(item.total),
                    style = AppTextStyles.Caption13.tabular(),
                    fontWeight = FontWeight.Medium,
                    color = scheme.onSurface,
                )
            }
        }
    }
}

private fun formatPercent(share: Float): String = NumberFormat.getPercentInstance(CzechLocale).format(share)
