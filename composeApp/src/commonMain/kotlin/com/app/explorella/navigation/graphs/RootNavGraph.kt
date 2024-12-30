package com.app.explorella.navigation.graphs

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import app.cash.sqldelight.db.SqlDriver
import com.app.explorella.navigation.Graph
import com.app.explorella.navigation.Routes
import com.app.explorella.screens.ItemDetailScreen
import com.app.explorella.screens.LocationSelectorScreen

@Composable
fun RootNavGraph(
    rootNavController: NavHostController,
    innerPadding: PaddingValues,
    sqlDriver: SqlDriver
) {
    NavHost(
        navController = rootNavController,
        startDestination = Graph.NAVIGATION_BAR_SCREEN_GRAPH,
        enterTransition = { fadeIn(initialAlpha = 0f, animationSpec = tween(durationMillis = 50)) },
        exitTransition = { fadeOut(animationSpec = tween(durationMillis = 50)) },
        popEnterTransition = {
            fadeIn(
                initialAlpha = 0f,
                animationSpec = tween(durationMillis = 50)
            )
        },
        popExitTransition = { fadeOut(animationSpec = tween(durationMillis = 50)) }
    ) {
        mainNavGraph(
            rootNavController = rootNavController,
            innerPadding = innerPadding,
            sqlDriver = sqlDriver
        )
        composable(route = Routes.ItemDetail.route) {
            rootNavController.previousBackStackEntry?.savedStateHandle?.get<Long>("itemId")
                ?.let { id ->
                    ItemDetailScreen(
                        rootNavController = rootNavController,
                        paddingValues = innerPadding,
                        sqlDriver = sqlDriver,
                        id = id
                    )
                }
        }
        composable(route = Routes.LocationSelector.route) {
            rootNavController.previousBackStackEntry?.savedStateHandle?.get<String>("locationQuery")
                ?.let { locationQuery ->
                    LocationSelectorScreen(
                        rootNavController = rootNavController,
                        paddingValues = innerPadding,
                        sqlDriver = sqlDriver,
                        locationQuery = locationQuery
                    )
                }
        }
    }
}