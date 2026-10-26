package com.example.financetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.example.financetracker.VydajnikApplication
import com.example.financetracker.data.repository.BASE_CURRENCY
import com.example.financetracker.data.repository.ExchangeRate
import com.example.financetracker.data.repository.ExchangeRateRepository
import com.example.financetracker.data.repository.convertToCzk
import com.example.financetracker.ui.theme.AppShapes
import com.example.financetracker.ui.theme.AppTextStyles
import com.example.financetracker.ui.theme.Dimens
import com.example.financetracker.ui.theme.Spacing
import com.example.financetracker.ui.theme.VydajnikTheme
import com.example.financetracker.ui.theme.tabular
import com.example.financetracker.ui.util.CzechLocale
import com.example.financetracker.ui.util.formatCzk
import com.example.financetracker.ui.util.formatDate
import com.example.financetracker.ui.util.parseAmount
import java.util.Currency
import kotlin.coroutines.cancellation.CancellationException

/**
 * Zadání částky s výběrem měny (formulář výdaje, výsledek skenu).
 * U cizí měny pod částkou živě ukazuje přepočet na Kč a odkud je kurz.
 *
 * Kurz si komponenta načítá sama (při změně měny a tlačítkem Obnovit) a posílá ho ven přes
 * [onRateChange] – ten se uloží k výdaji a částka v Kč se spočítá přes `convertToCzk(amount, rate)`.
 * `null` = kurz zatím není (načítá se nebo se nepodařil načíst) → uložení zakázat.
 * Pro CZK přijde kurz 1.0.
 *
 * [amount] je text tak, jak ho uživatel píše ("1234,5"), na číslo ho převede `parseAmount()`.
 * Chybu (např. prázdná částka) zobrazí [isError] + [errorText].
 */
@Composable
fun CurrencyAmountInput(
    amount: String,
    currency: String,
    onAmountChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onRateChange: (ExchangeRate?) -> Unit = {},
    label: String = "Částka",
    isError: Boolean = false,
    errorText: String? = null,
    enabled: Boolean = true,
    repository: ExchangeRateRepository = appExchangeRateRepository(),
) {
    var rateState by remember { mutableStateOf(RateState()) }
    var reloadRequest by remember { mutableIntStateOf(0) }
    val currentOnRateChange by rememberUpdatedState(onRateChange)

    LaunchedEffect(currency, reloadRequest, repository) {
        // Při obnovení stejné měny necháme starý kurz vidět, dokud nepřijde nový
        val previous = rateState.rate?.takeIf { it.currency == currency }
        rateState = RateState(rate = previous, isLoading = true)
        if (previous == null) currentOnRateChange(null)
        rateState = try {
            val rate = repository.getRateToCzk(currency)
            currentOnRateChange(rate)
            RateState(rate = rate)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            RateState(rate = previous)
        }
    }

    val scheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val border = when {
        isError -> BorderStroke(Dimens.TextFieldBorderFocused, scheme.error)
        focused -> BorderStroke(Dimens.TextFieldBorderFocused, scheme.primary)
        else -> null
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = scheme.surfaceContainerHigh,
        border = border,
    ) {
        Column(
            modifier = Modifier
                .padding(Dimens.AmountCardPadding)
                .alpha(if (enabled) 1f else DisabledAlpha),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = if (isError) scheme.error else scheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = amount,
                    onValueChange = { onAmountChange(sanitizeAmountInput(it)) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = Dimens.AmountInputHeight)
                        .semantics { contentDescription = label },
                    enabled = enabled,
                    textStyle = AppTextStyles.AmountInput.copy(color = scheme.onSurface),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    visualTransformation = AmountGroupingTransformation,
                    interactionSource = interactionSource,
                    cursorBrush = SolidColor(scheme.primary),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (amount.isEmpty()) {
                                Text("0", style = AppTextStyles.AmountInput, color = scheme.onSurfaceVariant)
                            }
                            innerTextField()
                        }
                    },
                )
                CurrencyButton(
                    currency = currency,
                    currencies = repository.supportedCurrencies,
                    onCurrencyChange = onCurrencyChange,
                    enabled = enabled,
                )
            }
            if (isError && errorText != null) {
                Text(errorText, style = MaterialTheme.typography.bodySmall, color = scheme.error)
            }

            if (currency != BASE_CURRENCY) {
                val rate = rateState.rate
                val amountCzk = if (rate != null) convertToCzk(parseAmount(amount) ?: 0.0, rate) else null
                Spacer(Modifier.height(Spacing.m))
                ConvertedAmount(amountCzk = amountCzk, isCached = rate?.isCached == true)
                val refresh: () -> Unit = { reloadRequest++ }
                Spacer(Modifier.height(Spacing.s))
                RateSourceRow(
                    rate = rate,
                    isRefreshing = rateState.isLoading,
                    onRefresh = if (enabled) refresh else null,
                )
            }
        }
    }
}

/**
 * Přepočtená částka v Kč. [isCached] = kurz je uložený z dřívějška (bez internetu) → „Orientační přepočet“.
 * [amountCzk] null = kurz ještě není.
 */
@Composable
fun ConvertedAmount(
    amountCzk: Double?,
    isCached: Boolean,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        Surface(modifier = Modifier.size(Dimens.Badge), shape = CircleShape, color = scheme.surface) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.CurrencyExchange,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(Dimens.BadgeIcon),
                )
            }
        }
        Column {
            Text(
                text = if (isCached) "Orientační přepočet" else "Přepočet na Kč",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
            Text(
                text = amountCzk?.let(::formatCzk) ?: "–",
                style = MaterialTheme.typography.headlineSmall.tabular(),
                color = if (isCached) scheme.onSurface else scheme.primary,
            )
        }
    }
}

/**
 * Řádek s kurzem a jeho zdrojem („1 EUR = 24,37 Kč“, „Frankfurter (ECB) · 26. 10. 2026“).
 * Uložený kurz (bez internetu) má výstražný rámeček. Použitelné i v Nastavení.
 * [rate] null + ![isRefreshing] = kurz se nepodařilo načíst.
 * [onRefresh] null = bez tlačítka Obnovit.
 */
@Composable
fun RateSourceRow(
    rate: ExchangeRate?,
    modifier: Modifier = Modifier,
    isRefreshing: Boolean = false,
    onRefresh: (() -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val semantic = VydajnikTheme.semantic
    val isCached = rate?.isCached == true

    val title = when {
        rate != null -> rateText(rate)
        isRefreshing -> "Načítám kurz…"
        else -> "Kurz se nepodařilo načíst"
    }
    val meta = when {
        rate == null && isRefreshing -> null
        rate == null -> "Zkontrolujte připojení k internetu"
        isCached -> "Bez připojení · uložený kurz z ${formatDate(rate.date)}"
        else -> "${rate.source} · ${formatDate(rate.date)}"
    }
    val metaColor = when {
        rate == null -> scheme.error
        isCached -> semantic.warning
        else -> scheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.RateRow,
        color = scheme.surface,
        border = if (isCached) BorderStroke(Dimens.TextFieldBorder, semantic.warning) else null,
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = Dimens.RateRowMinHeight)
                .padding(Dimens.RateRowPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.tabular(),
                    fontWeight = FontWeight.Medium,
                    color = scheme.onSurface,
                )
                if (meta != null) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isCached) FontWeight.SemiBold else null,
                        color = metaColor,
                    )
                }
            }
            if (onRefresh != null) {
                Box(Modifier.size(Dimens.IconButtonSize), contentAlignment = Alignment.Center) {
                    if (isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(Dimens.IconM), color = scheme.primary)
                    } else {
                        IconButton(onClick = onRefresh) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Obnovit kurz",
                                tint = scheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Tlačítko s kódem měny, po kliknutí nabídka měn. */
@Composable
private fun CurrencyButton(
    currency: String,
    currencies: List<String>,
    onCurrencyChange: (String) -> Unit,
    enabled: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilledTonalButton(
            onClick = { expanded = true },
            enabled = enabled,
            modifier = Modifier
                .height(Dimens.CurrencyButtonHeight)
                .semantics { contentDescription = "Měna ${currencyName(currency)}, změnit" },
            shape = AppShapes.Pill,
            contentPadding = Dimens.CurrencyButtonPadding,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = scheme.primaryContainer,
                contentColor = scheme.onPrimaryContainer,
            ),
        ) {
            Text(currency, style = MaterialTheme.typography.titleMedium)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            currencies.forEach { code ->
                DropdownMenuItem(
                    text = { Text("$code · ${currencyName(code)}") },
                    onClick = {
                        expanded = false
                        if (code != currency) onCurrencyChange(code)
                    },
                    trailingIcon = if (code == currency) {
                        { Icon(Icons.Filled.Check, contentDescription = "Vybráno") }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

/** Repozitář kurzů z [com.example.financetracker.AppContainer]. */
@Composable
private fun appExchangeRateRepository(): ExchangeRateRepository {
    val context = LocalContext.current.applicationContext
    return remember(context) { (context as VydajnikApplication).container.exchangeRateRepository }
}

private data class RateState(
    val rate: ExchangeRate? = null,
    val isLoading: Boolean = false,
)

private const val DisabledAlpha = 0.38f
private const val MaxIntegerDigits = 9
private const val MaxDecimals = 2
private const val GroupSeparator = ' '

/** „1 EUR = 24,37 Kč“; u levných měn za 100 nebo 1000 kusů („100 HUF = 6,63 Kč“). */
private fun rateText(rate: ExchangeRate): String {
    val units = when {
        rate.rateToCzk >= 1 -> 1
        rate.rateToCzk >= 0.01 -> 100
        else -> 1000
    }
    return "$units ${rate.currency} = ${formatCzk(rate.rateToCzk * units)}"
}

/** "EUR" → "Euro" (český název podle systému). */
private fun currencyName(code: String): String =
    runCatching { Currency.getInstance(code).getDisplayName(CzechLocale) }
        .getOrDefault(code)
        .replaceFirstChar { it.titlecase(CzechLocale) }

/**
 * Nechá jen číslice a jednu desetinnou čárku (tečka se převede na čárku),
 * max. 2 desetinná místa a 9 číslic před čárkou. "1.5" → "1,5", ",5" → "0,5", "007" → "7".
 */
internal fun sanitizeAmountInput(text: String): String {
    val integer = StringBuilder()
    val decimals = StringBuilder()
    var hasSeparator = false
    for (c in text) {
        when {
            c in '0'..'9' && !hasSeparator -> if (integer.length < MaxIntegerDigits) integer.append(c)
            c in '0'..'9' -> if (decimals.length < MaxDecimals) decimals.append(c)
            (c == ',' || c == '.') && !hasSeparator -> hasSeparator = true
        }
    }
    val integerPart = integer.toString().trimStart('0').ifEmpty { if (integer.isNotEmpty() || hasSeparator) "0" else "" }
    return if (hasSeparator) "$integerPart,$decimals" else integerPart
}

/** Zobrazí "12345,5" jako "12 345,5" (mezery jen na displeji, hodnota zůstává bez nich). */
private object AmountGroupingTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val integerEnd = raw.indexOf(',').let { if (it < 0) raw.length else it }
        val out = StringBuilder()
        val originalToTransformed = IntArray(raw.length + 1)
        val transformedToOriginal = ArrayList<Int>(raw.length + raw.length / 3 + 1)
        raw.forEachIndexed { i, c ->
            if (i in 1 until integerEnd && (integerEnd - i) % 3 == 0) {
                transformedToOriginal.add(i)
                out.append(GroupSeparator)
            }
            originalToTransformed[i] = out.length
            transformedToOriginal.add(i)
            out.append(c)
        }
        originalToTransformed[raw.length] = out.length
        transformedToOriginal.add(raw.length)

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                originalToTransformed[offset.coerceIn(0, raw.length)]

            override fun transformedToOriginal(offset: Int): Int =
                transformedToOriginal[offset.coerceIn(0, transformedToOriginal.lastIndex)]
        }
        return TransformedText(AnnotatedString(out.toString()), mapping)
    }
}
