package com.example.financetracker.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/*
 * Výdajník · Direction A "Material Expressive" · Phase 2
 * Source of truth: p2.css.
 *
 *  [p2.css]  = value copied 1:1 from p2.css (CSS var in comment)
 *  [derived] = role the mockups never show, so p2.css has no value; taken from the
 *              same tonal palette (seed #2B6A46). Change freely, nothing visible depends on it.
 *
 * CHROME roles below may be replaced by dynamic color on API 31+ (see Theme.kt).
 * SEMANTIC (error, warning) and CATEGORY colors are fixed and never dynamic.
 */

// ---------------------------------------------------------------- Light · static fallback
val LightColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF2B6A46),                 // --p        [p2.css]
    onPrimary = Color(0xFFFFFFFF),               // --onp      [p2.css]
    primaryContainer = Color(0xFFB1F1C4),        // --pc       [p2.css]
    onPrimaryContainer = Color(0xFF00210F),      // --onpc     [p2.css]
    inversePrimary = Color(0xFF96D5A9),          // --invp     [p2.css]
    secondary = Color(0xFF4F6354),               //            [derived]
    onSecondary = Color(0xFFFFFFFF),             //            [derived]
    secondaryContainer = Color(0xFFD2E8D5),      // --secc     [p2.css]
    onSecondaryContainer = Color(0xFF0C1F13),    // --onsecc   [p2.css]
    tertiary = Color(0xFF3A6471),                //            [derived]
    onTertiary = Color(0xFFFFFFFF),              //            [derived]
    tertiaryContainer = Color(0xFFBEEAF9),       // --tc       [p2.css]
    onTertiaryContainer = Color(0xFF001F27),     // --ontc     [p2.css]
    background = Color(0xFFF6FBF4),              // = --bg     [p2.css]
    onBackground = Color(0xFF171D19),            // = --on     [p2.css]
    surface = Color(0xFFF6FBF4),                 // --bg       [p2.css]
    onSurface = Color(0xFF171D19),               // --on       [p2.css]
    surfaceVariant = Color(0xFFDDE5DB),          //            [derived]
    onSurfaceVariant = Color(0xFF414942),        // --onv      [p2.css]
    surfaceTint = Color(0xFF2B6A46),             // = --p
    inverseSurface = Color(0xFF2C322D),          // --inv      [p2.css]
    inverseOnSurface = Color(0xFFEDF2EB),        // --oninv    [p2.css]
    error = Color(0xFFBA1A1A),                   // --err      [p2.css]
    onError = Color(0xFFFFFFFF),                 // --onerr    [p2.css]
    errorContainer = Color(0xFFFFDAD6),          // --errc     [p2.css]
    onErrorContainer = Color(0xFF410002),        // --onerrc   [p2.css]
    outline = Color(0xFF717971),                 // --ol       [p2.css]
    outlineVariant = Color(0xFFC1C9C0),          // --olv      [p2.css]
    scrim = Color(0xFF000000),                   // alpha applied per theme, see ScrimAlpha*
    surfaceBright = Color(0xFFF6FBF4),           //            [derived]
    surfaceDim = Color(0xFFD6DBD5),              //            [derived]
    surfaceContainerLowest = Color(0xFFFFFFFF),  // --sc0      [p2.css]
    surfaceContainerLow = Color(0xFFF0F5EE),     // --sc1      [p2.css]
    surfaceContainer = Color(0xFFEAEFE8),        // --sc2      [p2.css]
    surfaceContainerHigh = Color(0xFFE4EAE3),    // --sc3      [p2.css]
    surfaceContainerHighest = Color(0xFFDFE4DD), // --sc4      [p2.css]
)

// ---------------------------------------------------------------- Dark · static fallback
val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFF96D5A9),                 // --p        [p2.css]
    onPrimary = Color(0xFF00391E),               // --onp      [p2.css]
    primaryContainer = Color(0xFF0F5130),        // --pc       [p2.css]
    onPrimaryContainer = Color(0xFFB1F1C4),      // --onpc     [p2.css]
    inversePrimary = Color(0xFF2B6A46),          // --invp     [p2.css]
    secondary = Color(0xFFB6CCB9),               //            [derived]
    onSecondary = Color(0xFF213528),             //            [derived]
    secondaryContainer = Color(0xFF374B3D),      // --secc     [p2.css]
    onSecondaryContainer = Color(0xFFD2E8D5),    // --onsecc   [p2.css]
    tertiary = Color(0xFFA2CEDD),                //            [derived]
    onTertiary = Color(0xFF033542),              //            [derived]
    tertiaryContainer = Color(0xFF214C59),       // --tc       [p2.css]
    onTertiaryContainer = Color(0xFFBEEAF9),     // --ontc     [p2.css]
    background = Color(0xFF0F1511),              // = --bg     [p2.css]
    onBackground = Color(0xFFDFE4DD),            // = --on     [p2.css]
    surface = Color(0xFF0F1511),                 // --bg       [p2.css]
    onSurface = Color(0xFFDFE4DD),               // --on       [p2.css]
    surfaceVariant = Color(0xFF414942),          //            [derived]
    onSurfaceVariant = Color(0xFFC1C9C0),        // --onv      [p2.css]
    surfaceTint = Color(0xFF96D5A9),             // = --p
    inverseSurface = Color(0xFFDFE4DD),          // --inv      [p2.css]
    inverseOnSurface = Color(0xFF2C322D),        // --oninv    [p2.css]
    error = Color(0xFFFFB4AB),                   // --err      [p2.css]
    onError = Color(0xFF690005),                 // --onerr    [p2.css]
    errorContainer = Color(0xFF93000A),          // --errc     [p2.css]
    onErrorContainer = Color(0xFFFFDAD6),        // --onerrc   [p2.css]
    outline = Color(0xFF8B938A),                 // --ol       [p2.css]
    outlineVariant = Color(0xFF414942),          // --olv      [p2.css]
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF353A36),           //            [derived]
    surfaceDim = Color(0xFF0F1511),              //            [derived]
    surfaceContainerLowest = Color(0xFF0A0F0C),  // --sc0      [p2.css]
    surfaceContainerLow = Color(0xFF171D19),     // --sc1      [p2.css]
    surfaceContainer = Color(0xFF1B211D),        // --sc2      [p2.css]
    surfaceContainerHigh = Color(0xFF252B27),    // --sc3      [p2.css]
    surfaceContainerHighest = Color(0xFF303632), // --sc4      [p2.css]
)

/** --scrim in p2.css: rgba(0,0,0,0.32) light, rgba(0,0,0,0.6) dark. */
const val ScrimAlphaLight = 0.32f
const val ScrimAlphaDark = 0.6f

// ---------------------------------------------------------------- Semantic · fixed (never dynamic)
@Immutable
data class SemanticColors(
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    /** Cached-rate text/icon, outline of the cached rate row (offline). */
    val warning: Color,
    /** Offline banner background. */
    val warningContainer: Color,
    val onWarningContainer: Color,
)

val SemanticLight = SemanticColors(
    error = Color(0xFFBA1A1A),             // --err
    onError = Color(0xFFFFFFFF),           // --onerr
    errorContainer = Color(0xFFFFDAD6),    // --errc
    onErrorContainer = Color(0xFF410002),  // --onerrc
    warning = Color(0xFF7C5800),           // --warn
    warningContainer = Color(0xFFFFDEA8),  // --warnc
    onWarningContainer = Color(0xFF271900),// --onwarnc
)

val SemanticDark = SemanticColors(
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    warning = Color(0xFFF7BD48),
    warningContainer = Color(0xFF5D4200),
    onWarningContainer = Color(0xFFFFDEA8),
)

val LocalSemanticColors = staticCompositionLocalOf { SemanticLight }

// ---------------------------------------------------------------- Camera overlay · theme-independent
object CameraColors {
    val Background = Color(0xFF0B0F0C)            // .cam
    val Content = Color(0xFFFFFFFF)               // corners, text, icons
    val Scrim = Color(0x8C000000)                 // rgba(0,0,0,0.55) around viewfinder
    val Pill = Color(0x99000000)                  // .pill-d rgba(0,0,0,0.6)
    val ControlBackground = Color(0x29FFFFFF)     // .ib-d rgba(255,255,255,0.16)
    val Detected = Color(0xFF96D5A9)              // corners + highlight while reading
    val DetectedFill = Color(0x2E96D5A9)          // rgba(150,213,169,0.18)
    val Error = Color(0xFFFFB4AB)                 // corners on unreadable code
}
