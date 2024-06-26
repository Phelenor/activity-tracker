package com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rafaelboban.activitytracker.network.model.goals.ActivityGoalType
import com.rafaelboban.activitytracker.ui.screens.activity.ActivityState
import com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.components.ActivityChipRow
import com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.components.ActivityTabType
import com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.tabs.ActivityDetailsTab
import com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.tabs.ActivityGoalsTab
import com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.tabs.ActivityHeartRateTab
import com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.tabs.ActivityWeatherTab
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import kotlinx.collections.immutable.toImmutableList

@Composable
fun ActivityBottomSheetContent(
    state: ActivityState,
    scrollState: ScrollState,
    selectedTab: ActivityTabType,
    onTabSelected: (ActivityTabType) -> Unit,
    onLoadWeather: () -> Unit,
    onOpenAddGoal: () -> Unit,
    onRemoveGoal: (ActivityGoalType) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(selectedTab) {
        scrollState.scrollTo(0)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        ActivityChipRow(
            tabs = ActivityTabType.Individual.toImmutableList(),
            selectedTab = selectedTab,
            onTabSelected = onTabSelected
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 96.dp)
        ) {
            Crossfade(
                targetState = selectedTab,
                animationSpec = tween(200)
            ) { tab ->
                when (tab) {
                    ActivityTabType.DETAILS -> ActivityDetailsTab(
                        type = state.type,
                        status = state.status,
                        data = state.activityData,
                        duration = state.duration
                    )

                    ActivityTabType.HEART -> ActivityHeartRateTab(
                        status = state.status,
                        data = state.activityData,
                        duration = state.duration
                    )

                    ActivityTabType.GOALS -> ActivityGoalsTab(
                        state = state,
                        showAddButton = state.goals.size < ActivityGoalType.entries.size,
                        showAddGoalDialog = onOpenAddGoal,
                        onRemoveGoal = onRemoveGoal
                    )

                    ActivityTabType.WEATHER -> ActivityWeatherTab(
                        weather = state.weather,
                        isLoading = state.isWeatherLoading,
                        onReloadClick = onLoadWeather
                    )

                    else -> Unit
                }
            }
        }
    }
}

@Preview
@Composable
private fun ActivityBottomSheetContentPreview() {
    ActivityTrackerTheme {
        ActivityBottomSheetContent(
            state = ActivityState(type = ActivityType.WALK),
            scrollState = rememberScrollState(),
            selectedTab = ActivityTabType.DETAILS,
            onTabSelected = {},
            onLoadWeather = {},
            onOpenAddGoal = {},
            onRemoveGoal = {}
        )
    }
}
