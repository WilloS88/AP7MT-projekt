package com.example.financetracker.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* All values from p2.css (1 CSS px = 1 dp). */

object Spacing {
    val xxs = 4.dp    // .g4
    val xs = 6.dp     // .g6
    val s = 8.dp      // .g8
    val m = 12.dp     // .g12
    val l = 16.dp     // .g16, .mx16 screen margin
    val xl = 20.dp    // .bar.pl title inset, hero horizontal padding
    val xxl = 24.dp   // .g24, dialog padding
    val xxxl = 32.dp  // empty-state horizontal padding
    val ScreenMargin = 16.dp
}

object Dimens {
    // top app bar
    val TopBarHeight = 64.dp
    val TopBarHorizontalPadding = 4.dp
    val TopBarTitleInset = 20.dp          // when there is no navigation icon (.bar.pl)

    // Touch & icons
    val MinTouchTarget = 48.dp
    val IconButtonSize = 48.dp
    val Icon = 24.dp
    val IconM = 20.dp
    val IconS = 18.dp
    val IconXs = 16.dp
    val IconXxs = 14.dp

    // buttons
    val ButtonHeight = 40.dp
    val ButtonHorizontalPadding = 24.dp
    val TextButtonHorizontalPadding = 12.dp
    val ButtonIconGap = 8.dp
    val ButtonLargeHeight = 56.dp         // .b-xl (primary CTAs)
    val ButtonInlineHeight = 36.dp        // actions inside duplicate / error rows

    // FAB
    val FabHeight = 56.dp
    val FabPaddingStart = 16.dp
    val FabPaddingEnd = 20.dp
    val FabIconGap = 12.dp
    val FabMarginEnd = 16.dp
    val FabMarginBottomAboveNav = 16.dp   // bottom: 96 = nav 80 + 16

    // Cards
    val CardPadding = 16.dp               // .card
    val HeroPaddingVertical = 18.dp       // .hero (month total card)
    val HeroPaddingHorizontal = 20.dp
    val CategoryTileWidth = 108.dp        // category spending tile
    val CategoryTilePadding = 12.dp

    // Category badge (.cb)
    val Badge = 40.dp
    val BadgeIcon = 20.dp
    val BadgeCompact = 32.dp              // .cb.s32 / compact rows
    val BadgeCompactIcon = 18.dp
    val BadgeLarge = 56.dp                // .cb.s56
    val BadgeLargeIcon = 28.dp

    // chips
    val ChipHeight = 36.dp                // stock M3 FilterChip = 32 → Modifier.height(36.dp)
    val ChipPaddingStart = 10.dp
    val ChipPaddingEnd = 14.dp
    val ChipIconGap = 6.dp
    val ChipIcon = 18.dp
    val ChipBorder = 1.dp
    val ChipRowGap = 8.dp
    val MiniChipHeight = 24.dp            // .mini
    val MiniChipPaddingStart = 6.dp
    val MiniChipPaddingEnd = 8.dp
    val SourceBadgeHeight = 24.dp         // .src (source badge)
    val SourceBadgePaddingStart = 6.dp
    val SourceBadgePaddingEnd = 10.dp

    // day header · settings item · section header
    val DayHeaderHeight = 36.dp
    val SettingsItemMinHeight = 56.dp
    val SettingsItemPaddingVertical = 8.dp
    val SectionHeaderPadding = PaddingValues(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 8.dp)
    val DividerThickness = 1.dp

    // text field · amount · rate row · search
    val TextFieldHeight = 56.dp
    val TextFieldBorder = 1.dp
    val TextFieldBorderFocused = 2.dp
    val AmountCardPadding = PaddingValues(start = 20.dp, top = 16.dp, end = 16.dp, bottom = 12.dp)
    val AmountInputHeight = 56.dp
    val CurrencyButtonHeight = 48.dp
    val CurrencyButtonPadding = PaddingValues(start = 16.dp, end = 10.dp)
    val RateRowMinHeight = 48.dp
    val RateRowPadding = PaddingValues(start = 12.dp, top = 6.dp, bottom = 6.dp)
    val SearchHeight = 56.dp

    // navigation bar · segmented
    val NavBarHeight = 80.dp
    val NavBarPaddingTop = 12.dp
    val NavBarPaddingBottom = 16.dp
    val NavIndicatorWidth = 64.dp
    val NavIndicatorHeight = 32.dp
    val SegmentedHeight = 40.dp

    // banner · snackbar · dialog · sheet
    val BannerPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 12.dp)
    val SnackbarMinHeight = 48.dp
    val SnackbarHorizontalInset = 12.dp
    val DialogInset = 24.dp
    val DialogPadding = 24.dp
    val SheetHandleWidth = 32.dp
    val SheetHandleHeight = 4.dp
    val SheetHandleMarginTop = 22.dp
    val SheetHandleMarginBottom = 10.dp
    const val SheetHandleAlpha = 0.4f

    // progress · skeleton · empty state
    val LinearProgressHeight = 4.dp
    val EmptyStateHorizontalPadding = 32.dp
    val EmptyStateGap = 12.dp
    val EmptyArtWidth = 160.dp
    val EmptyArtHeight = 120.dp

    // donut · trend
    val DonutSize = 132.dp
    val DonutStroke = 20.dp
    val DonutGapDegrees = 3.3f            // 3 of 326.7 circumference units
    val TrendChartHeight = 150.dp
    val TrendBarWidth = 28.dp
    val TrendAverageStroke = 1.5.dp

    // scanner
    val ViewfinderSize = 260.dp
    val ViewfinderCornerLength = 44.dp
    val ViewfinderCornerStroke = 4.dp
    val DetectedHighlightStroke = 3.dp
    val ScanPillHeight = 40.dp
    val CameraControlSize = 56.dp

    // color swatch · icon picker
    val SwatchTarget = 48.dp
    val Swatch = 36.dp
    val SwatchSelectedRing = 2.dp
    val IconPickerCell = 48.dp
}

/* ---------------------------------------------------------------- List density */

@Immutable
data class ListDensity(
    val rowMinHeight: Dp,
    val badgeSize: Dp,
    val badgeIcon: Dp,
    val gap: Dp,
    val paddingStart: Dp,
    val paddingEnd: Dp,
    val headline: TextStyle,
    val supporting: TextStyle,
    val amount: TextStyle,
) {
    companion object {
        /** .li — Dashboard, Detail, Categories, Settings. */
        val Default = ListDensity(
            rowMinHeight = 64.dp, badgeSize = 40.dp, badgeIcon = 20.dp, gap = 16.dp,
            paddingStart = 16.dp, paddingEnd = 20.dp,
            headline = TextStyle(fontFamily = RobotoFlex, fontSize = 16.sp, lineHeight = 24.sp),
            supporting = TextStyle(fontFamily = RobotoFlex, fontSize = 14.sp, lineHeight = 20.sp),
            amount = AppTextStyles.RowAmount,
        )

        /** .li.cp — Expense list, Import preview. Row stays the 48 dp+ touch target. */
        val Compact = ListDensity(
            rowMinHeight = 52.dp, badgeSize = 32.dp, badgeIcon = 18.dp, gap = 12.dp,
            paddingStart = 16.dp, paddingEnd = 16.dp,
            headline = TextStyle(fontFamily = RobotoFlex, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
            supporting = TextStyle(fontFamily = RobotoFlex, fontSize = 12.sp, lineHeight = 16.sp),
            amount = AppTextStyles.RowAmountCompact,
        )
    }
}

/** Provide ListDensity.Compact around the Expense list and Import preview screens. */
val LocalListDensity = staticCompositionLocalOf { ListDensity.Default }
