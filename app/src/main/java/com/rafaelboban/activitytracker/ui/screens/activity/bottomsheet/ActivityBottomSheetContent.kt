package com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rafaelboban.activitytracker.ui.screens.activity.ActivityState
import com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.components.ActivityChipRow
import com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.components.ActivityTabType
import com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.tabs.ActivityDetailsTab
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme

@Composable
fun ActivityBottomSheetContent(
    state: ActivityState,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(ActivityTabType.DETAILS) }

    LaunchedEffect(selectedTab) {
        scrollState.scrollTo(0)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        ActivityChipRow(
            selectedTab = selectedTab,
            onTabSelected = { tab -> selectedTab = tab }
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
                    ActivityTabType.DETAILS -> ActivityDetailsTab(state = state)
                    ActivityTabType.HEART -> Box {}
                    ActivityTabType.GOALS -> Box {}
                    ActivityTabType.WEATHER -> Box {}
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
            state = ActivityState(activityType = ActivityType.WALK),
            scrollState = rememberScrollState()
        )
    }
}
