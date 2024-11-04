package com.app.explorella.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*


object Graph {
    const val NAVIGATION_BAR_SCREEN_GRAPH = "navigationBarScreenGraph"
}

sealed class Routes(var route: String) {
    data object Home : Routes("home")
    data object HomeDetail : Routes("homeDetail")

    data object Timeline : Routes("timeline")
    data object BucketList : Routes("bucketList")
    data object Todo : Routes("toDo")
    data object Map : Routes("map")
}

val navigationItemsLists = listOf(
    NavigationItem(
        unSelectedIcon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home,
        title = "Home",
        route = Routes.Home.route,
    ),
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
        route = Routes.BucketList.route,
    ),
    NavigationItem(
        unSelectedIcon = Icons.Outlined.Check,
        selectedIcon = Icons.Filled.Check,
        title = "Todo",
        route = Routes.Todo.route,
    ),
)