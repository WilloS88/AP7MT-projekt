package com.example.financetracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/** Settings → Vzhled → Motiv (radio group). */
enum class ThemeMode { System, Light, Dark }

private val LocalIsDarkTheme = staticCompositionLocalOf { false }

/** p2.css .prs = 14 % state layer of the content color when pressed. */
@OptIn(ExperimentalMaterial3Api::class)
private val VydajnikRipple = RippleConfiguration(
    rippleAlpha = RippleAlpha(
        draggedAlpha = 0.16f,
        focusedAlpha = 0.10f,
        hoveredAlpha = 0.08f,
        pressedAlpha = 0.14f,
    )
)

/**
 * Two color layers:
 *  - Chrome (MaterialTheme.colorScheme): dynamic on API 31+ when [dynamicColor] is on,
 *    otherwise the static green scheme from Color.kt.
 *  - Fixed layers that ignore the wallpaper: error/warning (SemanticColors) and the
 *    12-slot category palette (CategoryPalette).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VydajnikTheme(
    themeMode: ThemeMode = ThemeMode.System,
    dynamicColor: Boolean = true,          // Settings → "Barvy podle tapety"
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val semantic = if (dark) SemanticDark else SemanticLight

    val chrome: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        dark -> DarkColorScheme
        else -> LightColorScheme
    }.withFixedSemantic(semantic)

    CompositionLocalProvider(
        LocalCategoryPalette provides if (dark) CategoryPaletteDark else CategoryPaletteLight,
        LocalSemanticColors provides semantic,
        LocalIsDarkTheme provides dark,
        LocalRippleConfiguration provides VydajnikRipple,
    ) {
        MaterialTheme(
            colorScheme = chrome,
            typography = VydajnikTypography,
            shapes = VydajnikShapes,
            content = content,
        )
    }
}

/** Dynamic schemes ship their own error red; p2.css keeps error fixed, so override it. */
private fun ColorScheme.withFixedSemantic(s: SemanticColors): ColorScheme = copy(
    error = s.error,
    onError = s.onError,
    errorContainer = s.errorContainer,
    onErrorContainer = s.onErrorContainer,
)

/** Access point for everything MaterialTheme does not carry. */
object VydajnikTheme {
    val categories: CategoryPalette
        @Composable @ReadOnlyComposable get() = LocalCategoryPalette.current

    val semantic: SemanticColors
        @Composable @ReadOnlyComposable get() = LocalSemanticColors.current

    val isDark: Boolean
        @Composable @ReadOnlyComposable get() = LocalIsDarkTheme.current

    /** Scrim for dialogs and ModalBottomSheet(scrimColor = …): 32 % light, 60 % dark. */
    val scrim: Color
        @Composable @ReadOnlyComposable get() =
            Color.Black.copy(alpha = if (LocalIsDarkTheme.current) ScrimAlphaDark else ScrimAlphaLight)

    val listDensity: ListDensity
        @Composable @ReadOnlyComposable get() = LocalListDensity.current
}
