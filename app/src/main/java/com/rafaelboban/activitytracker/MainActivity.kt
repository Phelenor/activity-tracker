package com.rafaelboban.activitytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rafaelboban.activitytracker.ui.navigation.RootNavigation
import com.rafaelboban.activitytracker.ui.theme.ActivityTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ActivityTrackerTheme {
                RootNavigation()
            }
        }
    }
}
