package com.rafaelboban.activitytracker.ui.screens.main.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelboban.activitytracker.ui.theme.ActivityTrackerTheme

@Composable
fun DashboardScreenRoot(
    navigateToActivity: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    DashboardScreen(
        state = viewModel.state,
        onAction = { action ->
            when (action) {
                DashboardAction.OnActivityStartClick -> navigateToActivity()
            }
        }
    )
}

@Composable
fun DashboardScreen(
    state: DashboardState,
    onAction: (DashboardAction) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = { onAction(DashboardAction.OnActivityStartClick) }) {
            Text(text = "START ACTIVITY")
        }
    }
}

@Preview
@Composable
private fun DashboardScreenPreview() {
    ActivityTrackerTheme {
        DashboardScreen(
            state = DashboardState(isLoading = false),
            onAction = {}
        )
    }
}
