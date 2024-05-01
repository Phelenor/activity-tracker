package com.rafaelboban.activitytracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rafaelboban.activitytracker.ui.screens.login.LoginScreenRoot

@Composable
fun RootNavigation(
    skipLogin: Boolean,
    navHostController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navHostController,
        route = NavigationGraph.Root.route,
        startDestination = if (skipLogin) NavigationGraph.Main.route else NavigationGraph.Auth.route
    ) {
        composable(NavigationGraph.Auth.route) {
            LoginScreenRoot(
                onLoginSuccess = {
                    navHostController.navigate(NavigationGraph.Main.route) {
                        popUpTo(NavigationGraph.Auth.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(NavigationGraph.Main.route) {
            MainScreen(
                onLogout = {
                    navHostController.navigate(NavigationGraph.Auth.route) {
                        popUpTo(NavigationGraph.Main.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}

sealed class NavigationGraph(val route: String) {
    data object Root : NavigationGraph("root")
    data object Auth : NavigationGraph("auth")
    data object Main : NavigationGraph("main")
}
