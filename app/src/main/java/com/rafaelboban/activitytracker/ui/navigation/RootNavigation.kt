package com.rafaelboban.activitytracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rafaelboban.activitytracker.ui.auth.LoginScreen

@Composable
fun RootNavigation(navHostController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navHostController,
        route = NavigationGraph.Root.route,
        startDestination = NavigationGraph.Auth.route
    ) {
        composable(NavigationGraph.Auth.route) {
            LoginScreen()
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
