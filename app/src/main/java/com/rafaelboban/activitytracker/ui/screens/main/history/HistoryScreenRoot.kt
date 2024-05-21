package com.rafaelboban.activitytracker.ui.screens.main.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelboban.activitytracker.model.network.Activity
import com.rafaelboban.activitytracker.model.network.ActivityWeatherInfo
import com.rafaelboban.activitytracker.ui.components.ActivityCard
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import kotlinx.collections.immutable.toImmutableList
import java.time.Instant
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun HistoryScreenRoot(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    HistoryScreen(
        state = viewModel.state,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun HistoryScreen(
    state: HistoryState,
    onAction: (HistoryAction) -> Unit
) {
    val refreshState = rememberPullRefreshState(
        refreshing = state.isRefreshing,
        onRefresh = { onAction(HistoryAction.Refresh) }
    )

    Box(
        Modifier
            .fillMaxSize()
            .pullRefresh(refreshState)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 48.dp)
        ) {
            items(state.activities, key = { it.id }) { activity ->
                ActivityCard(
                    activity = activity,
                    onDeleteClick = { onAction(HistoryAction.DeleteActivity(activity.id)) },
                    navigateToActivityOverview = {},
                    modifier = Modifier
                        .animateItem()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        PullRefreshIndicator(
            modifier = Modifier.align(Alignment.TopCenter),
            refreshing = state.isRefreshing,
            state = refreshState,
            contentColor = MaterialTheme.colorScheme.primary
        )
    }
}

@PreviewLightDark
@Composable
private fun HistoryScreenPreview() {
    ActivityTrackerTheme {
        HistoryScreen(
            onAction = {},
            state = HistoryState(
                isRefreshing = false,
                activities = List(5) {
                    Activity(
                        id = Random.nextInt().toString(),
                        activityType = ActivityType.RUN,
                        durationSeconds = (10.minutes + 30.seconds).inWholeSeconds,
                        startTimestamp = Instant.now().epochSecond - 3.hours.inWholeSeconds,
                        distanceMeters = 2543,
                        avgSpeedKmh = 15.6234f,
                        elevation = 123,
                        imageUrl = null,
                        avgHeartRate = 120,
                        heartRateZoneDistribution = emptyMap(),
                        calories = 120,
                        goals = emptyList(),
                        endTimestamp = Instant.now().epochSecond,
                        maxHeartRate = 150,
                        maxSpeedKmh = 5.5f,
                        weather = ActivityWeatherInfo(
                            temp = 16f,
                            humidity = 84f,
                            icon = "04n",
                            description = "Overcast clouds"
                        )
                    )
                }.toImmutableList()
            )
        )
    }
}
