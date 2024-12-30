package com.app.explorella.navigation.graphs

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import app.cash.sqldelight.db.SqlDriver
import com.app.explorella.navigation.Graph
import com.app.explorella.navigation.Routes
import com.app.explorella.screens.MapScreen
import com.app.explorella.screens.TimelineScreen
import com.app.explorella.screens.ViewBucketScreen

fun NavGraphBuilder.mainNavGraph(
    rootNavController: NavHostController,
    innerPadding: PaddingValues,
    sqlDriver: SqlDriver
) {
    navigation(
        startDestination = Routes.Map.route,
        route = Graph.NAVIGATION_BAR_SCREEN_GRAPH
    ) {
        composable(route = Routes.ViewBuckets.route) {
            ViewBucketScreen(
                rootNavController = rootNavController,
                paddingValues = innerPadding,
                sqlDriver = sqlDriver
            )
        }
        composable(route = Routes.Timeline.route) {
            TimelineScreen(
                rootNavController = rootNavController,
                paddingValues = innerPadding,
                sqlDriver = sqlDriver
            )
        }
        composable(route = Routes.Map.route) {
            MapScreen(
                rootNavController = rootNavController,
                paddingValues = innerPadding,
                sqlDriver = sqlDriver
            )
        }
    }
}