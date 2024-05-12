package com.rafaelboban.activitytracker.wear.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.horologist.compose.ambient.AmbientAware
import com.google.android.horologist.compose.ambient.AmbientState
import com.google.android.horologist.compose.layout.AppScaffold
import com.rafaelboban.activitytracker.wear.service.ActivityTrackerService
import com.rafaelboban.activitytracker.wear.ui.activity.ActivityScreenRoot

@Composable
fun RootNavigation(
    navHostController: NavHostController = rememberSwipeDismissableNavController()
) {
    val context = LocalContext.current

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
                    ActivityScreenRoot(
                        toggleTrackerService = { shouldRun ->
                            if (shouldRun) {
                                context.startService(ActivityTrackerService.createStartIntent(context))
                            } else {
                                context.startService(ActivityTrackerService.createStopIntent(context))
                            }
                        }
                    )
                }
            }
        }
    }
}

sealed class Screens(val route: String) {

    data object Activity : Screens("activity")
}
