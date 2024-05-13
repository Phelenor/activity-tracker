package com.rafaelboban.activitytracker.wear.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.rafaelboban.activitytracker.wear.ui.navigation.RootNavigation
import com.rafaelboban.core.theme.wear.ActivityTrackerWearTheme
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

        viewModel.showSplashScreen()

        splashScreen.setKeepOnScreenCondition {
            viewModel.showSplashScreen
        }

        setContent {
            ActivityTrackerWearTheme {
                if (viewModel.showSplashScreen.not()) {
                    RootNavigation()
                }
            }
        }
    }
}
