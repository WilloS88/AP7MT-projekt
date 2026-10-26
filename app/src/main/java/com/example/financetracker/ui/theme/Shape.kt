package com.example.financetracker.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/*
 * Radii from p2.css. M3 components read these slots by default:
 *   FilterChip → small · Snackbar → extraSmall · AlertDialog, ModalBottomSheet → extraLarge
 *   Card → medium (16)   ← NOT what we want; pass shape explicitly (see AppShapes.Card)
 *   OutlinedTextField → extraSmall (8) ← pass AppShapes.TextField
 *   FAB / ExtendedFAB → large (20)     ← pass AppShapes.Fab (18)
 */
val VydajnikShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),   // .mini, .snk, .sk
    small = RoundedCornerShape(10.dp),       // .chip
    medium = RoundedCornerShape(16.dp),      // .tf, .bn, .rate, duplicate/error rows
    large = RoundedCornerShape(20.dp),       // .card.l (tiles, file header, scan-result info)
    extraLarge = RoundedCornerShape(28.dp),  // .card, .hero, .amt, .dlg, .sht (top)
)

object AppShapes {
    val Card = RoundedCornerShape(28.dp)              // .card / .hero / .amt
    val CardSmall = RoundedCornerShape(20.dp)         // .card.l
    val TextField = RoundedCornerShape(16.dp)         // .tf
    val Fab = RoundedCornerShape(18.dp)               // .fab
    val IconBox = RoundedCornerShape(14.dp)           // .cb.sq  (40 dp square badge)
    val Badge56 = RoundedCornerShape(18.dp)           // .cb.s56 (detail hero, category preview)
    val SuccessBadge = RoundedCornerShape(24.dp)      // import success 72 dp badge
    val Mini = RoundedCornerShape(8.dp)               // .mini
    val Chip = RoundedCornerShape(10.dp)              // .chip
    val Banner = RoundedCornerShape(16.dp)            // .bn
    val RateRow = RoundedCornerShape(16.dp)           // .rate
    val Snackbar = RoundedCornerShape(8.dp)           // .snk
    val Sheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp) // .sht
    val Viewfinder = RoundedCornerShape(28.dp)        // scanner viewfinder
    val Progress = RoundedCornerShape(2.dp)           // .pl
    val Skeleton = RoundedCornerShape(8.dp)           // .sk
    val TrendBar = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 6.dp, bottomEnd = 6.dp) // trend chart bar
    val Pill = CircleShape                            // buttons, segmented, search, nav indicator, .src, .cur
    val Badge = CircleShape                           // .cb 40 / .cb.s32
}
