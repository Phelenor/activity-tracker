package com.rafaelboban.activitytracker.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rafaelboban.activitytracker.ui.components.TrackerTopAppBar
import com.rafaelboban.activitytracker.ui.components.composableFade
import com.rafaelboban.activitytracker.ui.screens.main.dashboard.DashboardScreenRoot
import com.rafaelboban.activitytracker.ui.screens.main.profile.ProfileScreenRoot
import com.rafaelboban.activitytracker.ui.theme.Typography

sealed class MainScreen(val route: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector, val name: String) {
    data object History : MainScreen("history", Icons.Filled.DateRange, Icons.Outlined.DateRange, "History")
    data object Dashboard : MainScreen("dashboard", Icons.Filled.Home, Icons.Outlined.Home, "Dashboard")
    data object Profile : MainScreen("profile", Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle, "Profile")

    companion object {

        val Screens = linkedSetOf(History, Dashboard, Profile)

        fun getByRoute(route: String): MainScreen? {
            return listOf(History, Dashboard, Profile).find { it.route == route }
        }
    }
}

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    navigateToActivity: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = modifier,
        topBar = {
            TrackerTopAppBar(
                title = currentRoute?.let { MainScreen.getByRoute(it)?.name } ?: ""
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                MainScreen.Screens.forEach { item ->
                    val isSelected = currentRoute == item.route

                    NavigationBarItem(
                        colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.inversePrimary),
                        label = { Text(item.name) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                navController.graph.startDestinationRoute?.let { startRoute ->
                                    popUpTo(startRoute) {
                                        saveState = true
                                    }
                                }

                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.name
                            )
                        }
                    )
                }
            }
        }
    ) { padding ->
        MainNavigationGraph(
            modifier = Modifier.padding(padding),
            navController = navController,
            navigateToActivity = navigateToActivity,
            navigateToLogin = onLogout
        )
    }
}

@Composable
fun MainNavigationGraph(
    navController: NavHostController,
    navigateToLogin: () -> Unit,
    navigateToActivity: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        route = NavigationGraph.Main.route,
        startDestination = MainScreen.Dashboard.route
    ) {
        composableFade(route = MainScreen.History.route) {
            MockScreen(label = MainScreen.History.name, navigateUp = { navController.navigateUp() })
        }

        composableFade(route = MainScreen.Dashboard.route) {
            DashboardScreenRoot(navigateToActivity = navigateToActivity)
        }

        composableFade(route = MainScreen.Profile.route) {
            ProfileScreenRoot(
                navigateToLogin = navigateToLogin
            )
        }
    }
}

@Composable
fun MockScreen(
    label: String,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TrackerTopAppBar(
                title = label,
                showBackButton = true,
                onBackButtonClick = navigateUp
            )
        }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surfaceContainerLowest)
        ) {
            Text(
                text = label,
                style = Typography.displayLarge
            )
        }
    }
}
