package com.rafaelboban.activitytracker

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.rafaelboban.activitytracker.ui.AppViewModel
import com.rafaelboban.activitytracker.ui.navigation.RootNavigation
import com.rafaelboban.activitytracker.ui.theme.ActivityTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<AppViewModel>()

    @Inject
    lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
