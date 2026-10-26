package com.example.financetracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.financetracker.R

/*
 * Font: Roboto Flex (variable, SIL OFL 1.1) — same family as p2.css.
 *
 * How to include (bundled, works offline, gives the exact 650 weight):
 *   1. Download the variable TTF from https://fonts.google.com/specimen/Roboto+Flex
 *      (file "RobotoFlex[GRAD,XOPQ,XTRA,YOPQ,YTAS,YTDE,YTFI,YTLC,YTUC,opsz,slnt,wdth,wght].ttf").
 *   2. Rename to  app/src/main/res/font/roboto_flex.ttf  (lowercase, no brackets).
 *   3. Variable axes need API 26+. Below 26 every weight falls back to the file default (400).
 *
 * Downloadable Google Fonts (ui-text-google-fonts) also work, but only serve static
 * weights — 650 would snap to 600/700. Bundle the file if displaySmall must be exact.
 *
 * p2.css sets letter-spacing 0 everywhere except .dS (-0.8px), .amt-in (-0.5px), .lS (0.5px),
 * so M3 default tracking is overridden to 0.sp on purpose.
 */

/*
 * Zatím systémové písmo (Roboto), protože res/font/roboto_flex.ttf v projektu ještě není.
 * Po přidání souboru (návod výše) odkomentovat tento blok a smazat řádek
 * `val RobotoFlex: FontFamily = FontFamily.Default` pod ním:
 *
 * @OptIn(ExperimentalTextApi::class)
 * private fun robotoFlex(weight: Int) = Font(
 *     resId = R.font.roboto_flex,
 *     weight = FontWeight(weight),
 *     variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
 * )
 *
 * val RobotoFlex = FontFamily(robotoFlex(400), robotoFlex(500), robotoFlex(600), robotoFlex(650), robotoFlex(700))
 */
val RobotoFlex: FontFamily = FontFamily.Default

private val W650 = FontWeight(650)

val VydajnikTypography = Typography(
    // ---- not used in the mockups → M3 baseline sizes, our family [derived]
    displayLarge = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.Normal, fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp),
    displayMedium = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.Normal, fontSize = 45.sp, lineHeight = 52.sp, letterSpacing = 0.sp),
    headlineLarge = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.Normal, fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = 0.sp),

    // ---- p2.css
    displaySmall = TextStyle(fontFamily = RobotoFlex, fontWeight = W650, fontSize = 40.sp, lineHeight = 48.sp, letterSpacing = (-0.8).sp),           // .dS  month total, detail amount
    headlineMedium = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = 0.sp), // .hM
    headlineSmall = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = 0.sp),  // .hS  converted amount, dialog/empty titles
    titleLarge = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp),     // .tL  top app bar
    titleMedium = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.sp),    // .tM  section headers, tile amounts
    titleSmall = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.sp),     // .tS  form group labels
    bodyLarge = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.sp),        // .bL  row headline, inputs
    bodyMedium = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.sp),       // .bM  supporting text
    bodySmall = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.sp),        // .bS  meta, rate source
    labelLarge = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.sp),     // .lL / .btn
    labelMedium = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.sp),      // .lM  nav label, day header base
    labelSmall = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),   // .lS  overline labels
)

/** Extra styles used in p2.css that are outside the M3 scale. */
object AppTextStyles {
    /** .amt-in — amount input (Add / Edit / Scan result). */
    val AmountInput = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.SemiBold, fontSize = 44.sp, lineHeight = 56.sp, letterSpacing = (-0.5).sp, fontFeatureSettings = "tnum")

    /** 13 px inline text: category tile labels, delta chip, legend rows, stat insight. */
    val Caption13 = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.sp)

    /** .li .a — amount column in rows; compact density uses 14/20. */
    val RowAmount = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.sp, fontFeatureSettings = "tnum")
    val RowAmountCompact = RowAmount.copy(fontSize = 14.sp, lineHeight = 20.sp)

    /** .mono — raw CSV/PDF line in import error rows and row edit sheet. */
    val RawLine = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp)
}

/** .num — tabular figures for every amount, so decimal commas line up. */
fun TextStyle.tabular(): TextStyle = copy(fontFeatureSettings = "tnum")
