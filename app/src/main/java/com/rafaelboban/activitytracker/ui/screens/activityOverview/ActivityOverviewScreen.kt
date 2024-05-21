package com.rafaelboban.activitytracker.ui.screens.activityOverview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelboban.activitytracker.model.network.Activity
import com.rafaelboban.activitytracker.ui.components.ActivityDetailsCard
import com.rafaelboban.activitytracker.ui.components.ActivityMapCard
import com.rafaelboban.activitytracker.ui.components.LoadingIndicator
import com.rafaelboban.activitytracker.ui.components.TrackerTopAppBar
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme

@Composable
fun ActivityOverviewScreenRoot(
    navigateUp: () -> Boolean,
    viewModel: ActivityOverviewViewModel = hiltViewModel()
) {
    ActivityOverviewScreen(
        state = viewModel.state,
        onAction = { action ->
            when (action) {
                ActivityOverviewAction.OnBackClick -> navigateUp()
            }
        }
    )
}

@Composable
private fun ActivityOverviewScreen(
    state: ActivityOverviewState,
    onAction: (ActivityOverviewAction) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TrackerTopAppBar(
                title = state.activity?.activityType?.singularRes?.let { stringResource(it) } ?: "",
                showBackButton = true,
                onBackButtonClick = { onAction(ActivityOverviewAction.OnBackClick) }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            state.activity?.let { activity ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 4.dp, bottom = 48.dp)
                ) {
                    ActivityMapCard(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        imageUrl = activity.imageUrl,
                        startTimestamp = activity.startTimestamp,
                        weather = activity.weather
                    )

                    ActivityDetailsCard(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        activity = activity
                    )
                }
            }

            AnimatedVisibility(
                visible = state.isLoading,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200))
            ) {
                LoadingIndicator(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Preview
@Composable
private fun ActivityOverviewScreenPreview() {
    ActivityTrackerTheme {
        ActivityOverviewScreen(
            state = ActivityOverviewState(activity = Activity.MockModel),
            onAction = {}
        )
    }
}
