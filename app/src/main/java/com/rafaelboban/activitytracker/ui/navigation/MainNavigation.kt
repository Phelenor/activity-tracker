package com.rafaelboban.activitytracker.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Favorite
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
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rafaelboban.activitytracker.ui.components.TrackerTopAppBar
import com.rafaelboban.activitytracker.ui.screens.main.profile.ProfileScreenRoot
import com.rafaelboban.activitytracker.ui.theme.Typography

@Composable
fun MainNavigationGraph(
    navController: NavHostController,
    navigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        route = NavigationGraph.Main.route,
        startDestination = MainScreen.First.route
    ) {
        composable(MainScreen.First.route) {
            MockScreen(label = MainScreen.First.name)
        }

        composable(MainScreen.Second.route) {
            MockScreen(label = MainScreen.Second.name)
        }

        composable(MainScreen.Profile.route) {
            ProfileScreenRoot(
                navigateToLogin = navigateToLogin
            )
        }
    }
}

sealed class MainScreen(val route: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector, val name: String) {
    data object First : MainScreen("first", Icons.Filled.Favorite, Icons.Outlined.Favorite, "First")
    data object Second : MainScreen("second", Icons.Filled.Call, Icons.Outlined.Call, "Second")
    data object Profile : MainScreen("profile", Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle, "Profile")

    companion object {
        fun getByRoute(route: String): MainScreen? {
            return listOf(First, Second, Profile).find { it.route == route }
        }
    }
}

@Composable
fun MainScreen(
    onLogout: () -> Unit,
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
                listOf(
                    MainScreen.First,
                    MainScreen.Second,
                    MainScreen.Profile
                ).forEach { item ->
                    val selected = currentRoute == item.route

                    NavigationBarItem(
                        colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.inversePrimary),
                        label = { Text(item.name) },
                        selected = selected,
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
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
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
            navigateToLogin = onLogout
        )
    }
}

@Composable
fun MockScreen(
    label: String,
    modifier: Modifier = Modifier
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
