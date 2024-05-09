package com.rafaelboban.activitytracker.wear.ui.activity

import android.Manifest
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material3.ExperimentalWearMaterial3Api
import androidx.wear.compose.material3.HorizontalPageIndicator
import androidx.wear.compose.material3.rememberPageIndicatorState
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.rafaelboban.activitytracker.wear.ui.activity.tabs.HeartRateExercisePage
import com.rafaelboban.activitytracker.wear.ui.activity.tabs.MainExercisePage
import com.rafaelboban.activitytracker.wear.ui.activity.tabs.NoPhoneNearbyPage
import com.rafaelboban.core.theme.wear.ActivityTrackerWearTheme
import com.rafaelboban.core.tracker.utils.F
import kotlin.time.Duration

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ActivityScreenRoot(
    viewModel: ActivityViewModel = hiltViewModel()
) {
    val bodySensorsPermission = rememberPermissionState(
        permission = Manifest.permission.BODY_SENSORS,
        onPermissionResult = { granted ->
            if (granted) {
                viewModel.onAction(ActivityAction.GrantBodySensorsPermission)
            }
        }
    )

    LaunchedEffect(Unit) {
        if (bodySensorsPermission.status.isGranted.not()) {
            bodySensorsPermission.launchPermissionRequest()
        }
    }

    ActivityScreen(
        state = viewModel.state
    )
}

@OptIn(ExperimentalWearMaterial3Api::class)
@Composable
private fun ActivityScreen(
    state: ActivityState
) {
    val pageCount = if (state.canTrackHeartRate) 2 else 1
    var selectedPage by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(selectedPage) { pageCount }
    val animatedSelectedPage by animateFloatAsState(targetValue = selectedPage.F, label = "page_animation")
    val pageIndicatorState = rememberPageIndicatorState(pageCount) { animatedSelectedPage }

    LaunchedEffect(pagerState.currentPage) {
        selectedPage = pagerState.currentPage
    }

    if (state.isConnectedPhoneNearby) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = pagerState
            ) { page ->
                when (page) {
                    0 -> MainExercisePage(state = state)
                    1 -> HeartRateExercisePage(state = state)
                    else -> MainExercisePage(state = state)
                }
            }

            HorizontalPageIndicator(
                modifier = Modifier.padding(bottom = 4.dp),
                pageIndicatorState = pageIndicatorState
            )
        }
    } else {
        NoPhoneNearbyPage()
    }
}

@WearPreviewDevices
@Composable
private fun ActivityScreenPreview() {
    ActivityTrackerWearTheme {
        ActivityScreen(
            state = ActivityState(
                isConnectedPhoneNearby = true,
                canTrackHeartRate = true,
                duration = Duration.parse("1h 23m 35s"),
                distanceMeters = 32412,
                speed = 3.5f
            )
        )
    }
}
