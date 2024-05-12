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
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rafaelboban.activitytracker.R
import com.rafaelboban.activitytracker.ui.components.TrackerTopAppBar
import com.rafaelboban.activitytracker.ui.components.composableFade
import com.rafaelboban.activitytracker.ui.screens.main.dashboard.DashboardScreenRoot
import com.rafaelboban.activitytracker.ui.screens.main.profile.ProfileScreenRoot
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.shared.ui.util.UiText
import com.rafaelboban.core.theme.mobile.Typography
import kotlinx.serialization.Serializable

sealed interface MainScreenNavigation {

    @Serializable
    data object History : MainScreenNavigation

    @Serializable
    data object Dashboard : MainScreenNavigation

    @Serializable
    data object Profile : MainScreenNavigation

    companion object {

        val All = linkedSetOf(History, Dashboard, Profile)
    }
}

sealed class MainScreenBottomBarItem(val route: MainScreenNavigation, val selectedIcon: ImageVector, val unselectedIcon: ImageVector, val name: UiText) {
    data object History : MainScreenBottomBarItem(MainScreenNavigation.History, Icons.Filled.DateRange, Icons.Outlined.DateRange, UiText.StringResource(R.string.history))
    data object Dashboard : MainScreenBottomBarItem(MainScreenNavigation.Dashboard, Icons.Filled.Home, Icons.Outlined.Home, UiText.StringResource(R.string.dashboard))
    data object Profile : MainScreenBottomBarItem(MainScreenNavigation.Profile, Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle, UiText.StringResource(R.string.profile))

    companion object {

        val All = linkedSetOf(History, Dashboard, Profile)

        fun getByRoute(route: MainScreenNavigation) = All.first { it.route == route }
    }
}

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    navigateToActivity: (ActivityType) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = MainScreenNavigation.All.find { navBackStackEntry?.destination?.hasRoute(it::class) == true }

    Scaffold(
        modifier = modifier,
        topBar = {
            TrackerTopAppBar(
                title = currentRoute?.let { MainScreenBottomBarItem.getByRoute(it).name.asString() } ?: ""
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                MainScreenBottomBarItem.All.forEach { item ->
                    val isSelected = currentRoute == item.route

                    NavigationBarItem(
                        colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.inversePrimary),
                        label = { Text(item.name.asString()) },
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
                                contentDescription = item.name.asString()
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
    navigateToActivity: (ActivityType) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = MainScreenNavigation.Dashboard
    ) {
        composableFade<MainScreenNavigation.History> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.surfaceContainerLowest)
            ) {
                Text(
                    text = "History",
                    style = Typography.displayLarge
                )
            }
        }

        composableFade<MainScreenNavigation.Dashboard> {
            DashboardScreenRoot(navigateToActivity = navigateToActivity)
        }

        composableFade<MainScreenNavigation.Profile> {
            ProfileScreenRoot(
                navigateToLogin = navigateToLogin
            )
        }
    }
}
