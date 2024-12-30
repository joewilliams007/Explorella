package com.app.explorella.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Star


object Graph {
    const val NAVIGATION_BAR_SCREEN_GRAPH = "navigationBarScreenGraph"
}

sealed class Routes(var route: String) {
    data object ItemDetail : Routes("itemDetail")
    data object AddBuckets : Routes("addBuckets")
    data object Timeline : Routes("timeline")
    data object ViewBuckets : Routes("viewBuckets")
    data object Todo : Routes("toDo")
    data object Map : Routes("map")
    data object LocationPickerScreen : Routes("locationPickerScreen")
    data object LocationSelector : Routes("locationSelector")
}

val navigationItemsLists = listOf(
    NavigationItem(
        unSelectedIcon = Icons.Outlined.Favorite,
        selectedIcon = Icons.Filled.Favorite,
        title = "Timeline",
        route = Routes.Timeline.route,
    ),
    NavigationItem(
        unSelectedIcon = Icons.Outlined.Place,
        selectedIcon = Icons.Filled.Place,
        title = "Map",
        route = Routes.Map.route,
    ),
    NavigationItem(
        unSelectedIcon = Icons.Outlined.Star,
        selectedIcon = Icons.Filled.Star,
        title = "Bucket list",
        route = Routes.ViewBuckets.route,
    )
)