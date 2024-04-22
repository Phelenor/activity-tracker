package com.rafaelboban.activitytracker.ui.navigation

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun RootNavigation(navHostController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navHostController,
        route = NavigationGraph.Root.route,
        startDestination = NavigationGraph.Auth.route
    ) {
        composable(NavigationGraph.Auth.route) {
            MockScreen(
                label = "Auth",
                modifier = Modifier
                    .statusBarsPadding()
                    .navigationBarsPadding()
            )
        }

        composable(NavigationGraph.Main.route) {
            MainScreen()
        }
    }
}

sealed class NavigationGraph(val route: String) {
    data object Root : NavigationGraph("root")
    data object Auth : NavigationGraph("auth")
    data object Main : NavigationGraph("main")
}
