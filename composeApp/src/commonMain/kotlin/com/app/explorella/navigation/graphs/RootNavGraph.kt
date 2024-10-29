package com.app.explorella.navigation.graphs

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.app.explorella.navigation.Graph
import com.app.explorella.navigation.Routes
import com.app.explorella.screens.HomeDetailScreen

@Composable
fun RootNavGraph(
    rootNavController: NavHostController,
    innerPadding: PaddingValues
) {
    NavHost(
        navController = rootNavController,
        startDestination = Graph.NAVIGATION_BAR_SCREEN_GRAPH,
    ) {
        mainNavGraph(rootNavController = rootNavController, innerPadding = innerPadding)
        composable(
            route = Routes.HomeDetail.route,
        ) {
            rootNavController.previousBackStackEntry?.savedStateHandle?.get<String>("name")?.let { name ->
                HomeDetailScreen(rootNavController = rootNavController, name = name)
            }
        }
    }
}