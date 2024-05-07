package com.rafaelboban.activitytracker.ui.screens.activity

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelboban.activitytracker.R
import com.rafaelboban.activitytracker.model.ActivityData
import com.rafaelboban.activitytracker.model.ActivityType
import com.rafaelboban.activitytracker.tracking.service.ActivityTrackerService
import com.rafaelboban.activitytracker.ui.components.ActivityDataColumn
import com.rafaelboban.activitytracker.ui.components.ActivityFloatingActionButton
import com.rafaelboban.activitytracker.ui.components.DialogScaffold
import com.rafaelboban.activitytracker.ui.components.InfoDialog
import com.rafaelboban.activitytracker.ui.components.map.ActivityTrackerMap
import com.rafaelboban.activitytracker.ui.screens.activity.ActivityStatus.Companion.isRunning
import com.rafaelboban.activitytracker.ui.screens.activity.components.ActivityTopAppBar
import com.rafaelboban.activitytracker.ui.theme.ActivityTrackerTheme
import com.rafaelboban.activitytracker.ui.util.ObserveAsEvents
import com.rafaelboban.activitytracker.util.ActivityDataFormatter
import com.rafaelboban.activitytracker.util.ActivityDataFormatter.formatElapsedTimeDisplay
import com.rafaelboban.activitytracker.util.ActivityDataFormatter.roundToDecimals
import kotlin.time.Duration

@Composable
fun ActivityScreenRoot(
    navigateUp: () -> Boolean,
    viewModel: ActivityViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            ActivityEvent.NavigateBack -> navigateUp()
        }
    }

    ActivityScreen(
        state = viewModel.state,
        toggleTrackerService = { shouldRun ->
            if (shouldRun) {
                context.startService(ActivityTrackerService.createStartIntent(context))
            } else {
                context.startService(ActivityTrackerService.createStopIntent(context))
            }
        },
        onAction = { action ->
            if (action == ActivityAction.OnBackClick) {
                if (viewModel.state.activityStatus.isRunning.not()) {
                    navigateUp()
                }
            }

            viewModel.onAction(action)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    state: ActivityState,
    onAction: (ActivityAction) -> Unit,
    toggleTrackerService: (Boolean) -> Unit
) {
    val density = LocalDensity.current
    val navigationBarPadding = with(density) { WindowInsets.navigationBars.getBottom(density).toDp() }

    LaunchedEffect(state.activityStatus) {
        if (state.activityStatus == ActivityStatus.IN_PROGRESS && ActivityTrackerService.isActive.not()) {
            toggleTrackerService(true)
        }

        if (state.activityStatus == ActivityStatus.FINISHED && ActivityTrackerService.isActive) {
            toggleTrackerService(false)
        }
    }

    DialogScaffold(
        showDialog = state.showDiscardDialog,
        onDismiss = { onAction(ActivityAction.DismissDiscardDialog) }
    ) {
        InfoDialog(
            title = stringResource(id = R.string.discard_activity),
            subtitle = stringResource(id = R.string.discard_activity_info),
            actionText = stringResource(id = R.string.discard),
            actionButtonColor = MaterialTheme.colorScheme.error,
            actionButtonTextColor = MaterialTheme.colorScheme.onError,
            onDismissClick = { onAction(ActivityAction.DismissDiscardDialog) },
            onActionClick = {
                toggleTrackerService(false)
                onAction(ActivityAction.DiscardActivity)
            }
        )
    }

    BottomSheetScaffold(
        sheetPeekHeight = 36.dp + navigationBarPadding,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        sheetContent = {
            Spacer(modifier = Modifier.height(300.dp))
        }
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxWidth()
        ) {
            val (infoCard, map, controls) = createRefs()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .zIndex(1f)
                    .constrainAs(infoCard) {
                        top.linkTo(parent.top)
                        width = Dimension.matchParent
                    }
            ) {
                ActivityTopAppBar(
                    activityType = ActivityType.RUN,
                    onBackClick = { onAction(ActivityAction.OnBackClick) },
                    gpsOk = state.currentLocation != null
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 12.dp)
                ) {
                    ActivityDataColumn(
                        modifier = Modifier.weight(1f),
                        title = stringResource(id = R.string.duration),
                        value = state.duration.formatElapsedTimeDisplay(),
                        icon = Icons.Outlined.Timer
                    )

                    VerticalDivider(modifier = Modifier.height(24.dp))

                    ActivityDataColumn(
                        modifier = Modifier.weight(1f),
                        title = stringResource(id = R.string.distance),
                        value = ActivityDataFormatter.formatDistanceDisplay(state.activityData.distanceMeters),
                        unit = if (state.activityData.distanceMeters < 1000) "m" else "km",
                        icon = Icons.AutoMirrored.Outlined.TrendingUp
                    )

                    VerticalDivider(modifier = Modifier.height(24.dp))

                    ActivityDataColumn(
                        modifier = Modifier.weight(1f),
                        title = stringResource(id = R.string.speed),
                        value = state.activityData.speed.roundToDecimals(1),
                        unit = "km/h",
                        icon = Icons.Outlined.Speed
                    )
                }
            }

            ActivityTrackerMap(
                currentLocation = state.currentLocation,
                locations = state.activityData.locations,
                modifier = Modifier.constrainAs(map) {
                    top.linkTo(infoCard.bottom, margin = (-16).dp)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.matchParent
                    height = Dimension.fillToConstraints
                }
            )

            if (state.activityStatus != ActivityStatus.FINISHED) {
                AnimatedContent(
                    targetState = state.activityStatus,
                    label = "controls",
                    transitionSpec = {
                        slideInVertically(
                            animationSpec = tween(200),
                            initialOffsetY = { it }
                        ) togetherWith slideOutVertically(
                            animationSpec = tween(200),
                            targetOffsetY = { it }
                        )
                    },
                    modifier = Modifier
                        .padding(bottom = 42.dp)
                        .zIndex(1f)
                        .constrainAs(controls) {
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            width = Dimension.value(152.dp)
                        }
                ) { status ->
                    when (status) {
                        ActivityStatus.NOT_STARTED -> {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                ActivityFloatingActionButton(
                                    icon = Icons.Filled.PlayArrow,
                                    onClick = { onAction(ActivityAction.OnStartClick) }
                                )
                            }
                        }

                        ActivityStatus.IN_PROGRESS -> {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                ActivityFloatingActionButton(
                                    icon = Icons.Filled.Pause,
                                    onClick = { onAction(ActivityAction.OnPauseClick) }
                                )
                            }
                        }

                        ActivityStatus.PAUSED -> {
                            Row {
                                ActivityFloatingActionButton(
                                    icon = Icons.Filled.PlayArrow,
                                    onClick = { onAction(ActivityAction.OnResumeClick) }
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                ActivityFloatingActionButton(
                                    icon = ImageVector.vectorResource(id = R.drawable.ic_finish_flag),
                                    onClick = { onAction(ActivityAction.OnFinishClick) }
                                )
                            }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 360)
@PreviewLightDark
@Composable
private fun ActivityScreenPreview() {
    ActivityTrackerTheme {
        ActivityScreen(
            onAction = {},
            toggleTrackerService = {},
            state = ActivityState(
                duration = Duration.parse("1h 30m 52s"),
                activityData = ActivityData(distanceMeters = 1925, speed = 9.2f)
            )
        )
    }
}