package com.rafaelboban.activitytracker.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.rafaelboban.activitytracker.ui.navigation.RootNavigation
import com.rafaelboban.activitytracker.ui.theme.ActivityTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setOnExitAnimationListener { provider ->
            provider.view.animate().alpha(0f).setDuration(200).withEndAction {
                provider.remove()
            }
        }

        viewModel.initAndSplashDelay()

        splashScreen.setKeepOnScreenCondition {
            viewModel.state.isCheckingToken
        }

        setContent {
            if (viewModel.state.isCheckingToken.not()) {
                ActivityTrackerTheme {
                    enableEdgeToEdge(
                        statusBarStyle = if (isSystemInDarkTheme()) {
                            SystemBarStyle.light(
                                MaterialTheme.colorScheme.primary.toArgb(),
                                MaterialTheme.colorScheme.primary.toArgb()
                            )
                        } else {
                            SystemBarStyle.dark(
                                MaterialTheme.colorScheme.primary.toArgb()
                            )
                        },
                        navigationBarStyle = if (!isSystemInDarkTheme()) {
                            SystemBarStyle.light(
                                MaterialTheme.colorScheme.inversePrimary.toArgb(),
                                MaterialTheme.colorScheme.inversePrimary.toArgb()
                            )
                        } else {
                            SystemBarStyle.dark(
                                MaterialTheme.colorScheme.inversePrimary.toArgb()
                            )
                        }
                    )

                    RootNavigation(
                        skipLogin = viewModel.state.isLoggedIn
                    )
                }
            }
        }
    }
}
