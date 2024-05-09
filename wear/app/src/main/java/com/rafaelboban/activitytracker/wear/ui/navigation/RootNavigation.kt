package com.rafaelboban.activitytracker.wear.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.horologist.compose.ambient.AmbientAware
import com.google.android.horologist.compose.ambient.AmbientState
import com.google.android.horologist.compose.layout.AppScaffold
import com.rafaelboban.activitytracker.wear.ui.activity.ActivityScreenRoot

@Composable
fun RootNavigation(
    navHostController: NavHostController = rememberSwipeDismissableNavController()
) {
    AmbientAware(isAlwaysOnScreen = true) { ambientStateUpdate ->
        AppScaffold(
            timeText = {
                if (ambientStateUpdate.ambientState is AmbientState.Interactive) {
                    TimeText {
                        time()
                    }
                }
            }
        ) {
            NavHost(
                navController = navHostController,
                startDestination = Screens.Activity.route
            ) {
                composable(Screens.Activity.route) {
                    ActivityScreenRoot()
                }
            }
        }
    }
}

sealed class Screens(val route: String) {

    data object Activity : Screens("activity")
}
