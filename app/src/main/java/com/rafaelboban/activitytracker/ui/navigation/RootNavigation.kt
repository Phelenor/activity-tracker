package com.rafaelboban.activitytracker.ui.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.rafaelboban.activitytracker.ui.components.composableSlide
import com.rafaelboban.activitytracker.ui.screens.activity.ActivityScreenRoot
import com.rafaelboban.activitytracker.ui.screens.activityOverview.ActivityOverviewScreenRoot
import com.rafaelboban.activitytracker.ui.screens.camera.ScannerScreenRoot
import com.rafaelboban.activitytracker.ui.screens.login.LoginScreenRoot
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
                navigateToActivityOverview = { id ->
                    navHostController.navigate(NavigationGraph.ActivityOverview(id))
                },
                navigateToQRCodeScanner = { scannerType ->
                    navHostController.navigate(NavigationGraph.QRCodeScanner(scannerType.ordinal))
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
        ) {
            ActivityScreenRoot(
                navigateUp = { navHostController.navigateUp() }
            )
        }

        composableSlide<NavigationGraph.ActivityOverview> {
            ActivityOverviewScreenRoot(
                navigateUp = { navHostController.navigateUp() }
            )
        }

        composableSlide<NavigationGraph.QRCodeScanner> {
            val context = LocalContext.current

            ScannerScreenRoot(
                navigateUp = { navHostController.navigateUp() },
                navigateToGroupActivity = {
                    Toast.makeText(context, "Successful join activity", Toast.LENGTH_LONG).show()
                }
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

    @Serializable
    data class ActivityOverview(val id: String) : NavigationGraph

    @Serializable
    data class QRCodeScanner(val scannerTypeOrdinal: Int) : NavigationGraph
}
