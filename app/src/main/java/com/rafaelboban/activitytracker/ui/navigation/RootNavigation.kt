package com.rafaelboban.activitytracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.rafaelboban.activitytracker.ui.components.composableSlide
import com.rafaelboban.activitytracker.ui.screens.activity.ActivityScreenRoot
import com.rafaelboban.activitytracker.ui.screens.login.LoginScreenRoot
import com.rafaelboban.core.shared.model.ActivityType
import kotlinx.serialization.Serializable

@Composable
fun RootNavigation(
    skipLogin: Boolean,
    navHostController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navHostController,
        startDestination = if (skipLogin) NavigationGraph.Main else NavigationGraph.Auth
    ) {
        composableSlide<NavigationGraph.Auth> {
            LoginScreenRoot(
                onLoginSuccess = {
                    navHostController.navigate(NavigationGraph.Main) {
                        popUpTo(NavigationGraph.Auth) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composableSlide<NavigationGraph.Main> {
            MainScreen(
                navigateToActivity = { type ->
                    navHostController.navigate(NavigationGraph.Activity(type.ordinal))
                },
                onLogout = {
                    navHostController.navigate(NavigationGraph.Auth) {
                        popUpTo(NavigationGraph.Main) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composableSlide<NavigationGraph.Activity>(
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "activity_tracker://current_activity/{activityTypeOrdinal}"
                }
            )
        ) { backStackEntry ->
            val activityTypeOrdinal = backStackEntry.toRoute<NavigationGraph.Activity>().activityTypeOrdinal
            val activityType = ActivityType.entries[activityTypeOrdinal]

            ActivityScreenRoot(
                activityType = activityType,
                navigateUp = { navHostController.navigateUp() }
            )
        }
    }
}

sealed interface NavigationGraph {

    @Serializable
    data object Auth : NavigationGraph

    @Serializable
    data object Main : NavigationGraph

    @Serializable
    data class Activity(val activityTypeOrdinal: Int) : NavigationGraph
}
