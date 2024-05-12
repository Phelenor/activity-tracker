package com.rafaelboban.activitytracker.wear.ui.activity

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material3.ExperimentalWearMaterial3Api
import androidx.wear.compose.material3.HorizontalPageIndicator
import androidx.wear.compose.material3.rememberPageIndicatorState
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.rafaelboban.activitytracker.wear.service.ActivityTrackerService
import com.rafaelboban.activitytracker.wear.ui.activity.tabs.HeartRateExercisePage
import com.rafaelboban.activitytracker.wear.ui.activity.tabs.MainExercisePage
import com.rafaelboban.activitytracker.wear.ui.activity.tabs.NoPhoneNearbyPage
import com.rafaelboban.activitytracker.wear.ui.activity.tabs.OpenActivityOnPhonePage
import com.rafaelboban.core.shared.ui.util.ObserveAsEvents
import com.rafaelboban.core.shared.utils.F
import com.rafaelboban.core.theme.wear.ActivityTrackerWearTheme
import kotlin.time.Duration

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ActivityScreenRoot(
    viewModel: ActivityViewModel = hiltViewModel(),
    toggleTrackerService: (Boolean) -> Unit
) {
    val context = LocalContext.current

    val bodySensorsPermissions = rememberMultiplePermissionsState(
        permissions = listOfNotNull(
            Manifest.permission.BODY_SENSORS,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS else null
        ),
        onPermissionsResult = { result ->
            if (result[Manifest.permission.BODY_SENSORS] == true) {
                viewModel.onAction(ActivityAction.GrantBodySensorsPermission)
            }
        }
    )

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            is ActivityEvent.Error -> Toast.makeText(context, event.message.asString(context), Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (bodySensorsPermissions.allPermissionsGranted.not()) {
            bodySensorsPermissions.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(viewModel.state.isActive) {
        if (viewModel.state.isActive && ActivityTrackerService.isActive.not()) {
            toggleTrackerService(true)
        }

        if (viewModel.state.isActive.not() && ActivityTrackerService.isActive) {
            toggleTrackerService(false)
        }
    }

    ActivityScreen(
        state = viewModel.state,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalWearMaterial3Api::class)
@Composable
private fun ActivityScreen(
    state: ActivityState,
    onAction: (ActivityAction) -> Unit
) {
    val pageCount = if (state.canTrackHeartRate) 2 else 1
    var selectedPage by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(selectedPage) { pageCount }
    val animatedSelectedPage by animateFloatAsState(targetValue = selectedPage.F, label = "page_animation")
    val pageIndicatorState = rememberPageIndicatorState(pageCount) { animatedSelectedPage }

    val startPage by remember(state.isConnectedPhoneNearby, state.canTrack) {
        derivedStateOf {
            when {
                !state.isConnectedPhoneNearby -> 0
                !state.canTrack -> 1
                else -> 2
            }
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        selectedPage = pagerState.currentPage
    }

    Crossfade(
        targetState = startPage,
        animationSpec = tween(350),
        label = "main_screen_crossfade"
    ) { key ->
        when (key) {
            0 -> {
                NoPhoneNearbyPage()
            }

            1 -> {
                OpenActivityOnPhonePage(
                    openOnPhone = { onAction(ActivityAction.OpenAppOnPhone) }
                )
            }

            else -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    HorizontalPager(
                        modifier = Modifier.fillMaxSize(),
                        state = pagerState
                    ) { page ->
                        when (page) {
                            1 -> HeartRateExercisePage(state = state)
                            else -> MainExercisePage(
                                state = state,
                                onAction = onAction
                            )
                        }
                    }

                    HorizontalPageIndicator(
                        modifier = Modifier.padding(bottom = 4.dp),
                        pageIndicatorState = pageIndicatorState
                    )
                }
            }
        }
    }
}

@WearPreviewDevices
@Composable
private fun ActivityScreenPreview() {
    ActivityTrackerWearTheme {
        ActivityScreen(
            onAction = {},
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
