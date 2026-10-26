package com.example.financetracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.TheaterComedy
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Ikony kategorií. V databázi je jen klíč (Category.iconKey), tady se převádí na ikonu.
 * Pořadí = pořadí ve výběru ikony při vytváření kategorie.
 */
val CategoryIcons: Map<String, ImageVector> = linkedMapOf(
    "food" to Icons.Outlined.Restaurant,
    "home" to Icons.Outlined.Home,
    "transport" to Icons.Outlined.DirectionsBus,
    "fun" to Icons.Outlined.TheaterComedy,
    "shopping" to Icons.Outlined.ShoppingBag,
    "health" to Icons.Outlined.LocalHospital,
    "other" to Icons.Outlined.MoreHoriz,
    "coffee" to Icons.Outlined.LocalCafe,
    "car" to Icons.Outlined.DirectionsCar,
    "travel" to Icons.Outlined.Flight,
    "education" to Icons.Outlined.School,
    "sport" to Icons.Outlined.FitnessCenter,
    "gift" to Icons.Outlined.CardGiftcard,
    "pets" to Icons.Outlined.Pets,
    "kids" to Icons.Outlined.ChildCare,
    "bills" to Icons.AutoMirrored.Outlined.ReceiptLong,
    "phone" to Icons.Outlined.Smartphone,
)

/** Neznámý klíč → ikona „Ostatní“. */
fun categoryIcon(iconKey: String): ImageVector = CategoryIcons[iconKey] ?: Icons.Outlined.MoreHoriz
