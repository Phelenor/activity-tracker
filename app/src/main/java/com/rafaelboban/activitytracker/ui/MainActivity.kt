package com.rafaelboban.activitytracker.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.rafaelboban.activitytracker.ui.navigation.RootNavigation
import com.rafaelboban.activitytracker.ui.theme.ActivityTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setOnExitAnimationListener { provider ->
            provider.view.animate().alpha(0f).setDuration(200).withEndAction {
                provider.remove()
            }
        }

        viewModel.initAndSplashDelay()

        splashScreen.setKeepOnScreenCondition {
            viewModel.showSplashScreen.value
        }

        setContent {
            ActivityTrackerTheme {
                RootNavigation(
                    skipLogin = viewModel.isAuthTokenValid
                )
            }
        }
    }
}
