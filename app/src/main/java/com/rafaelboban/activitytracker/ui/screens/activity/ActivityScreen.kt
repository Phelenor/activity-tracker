@file:OptIn(ExperimentalMaterial3Api::class)

package com.rafaelboban.activitytracker.ui.screens.activity

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelboban.activitytracker.model.ActivityData
import com.rafaelboban.activitytracker.service.ActivityTrackerService
import com.rafaelboban.activitytracker.ui.components.ActivityDataColumn
import com.rafaelboban.activitytracker.ui.components.ActivityFloatingActionButton
import com.rafaelboban.activitytracker.ui.components.DialogScaffold
import com.rafaelboban.activitytracker.ui.components.InfoDialog
import com.rafaelboban.activitytracker.ui.components.SelectMapTypeDialog
import com.rafaelboban.activitytracker.ui.components.SetActivityGoalsDialog
import com.rafaelboban.activitytracker.ui.components.map.ActivityTrackerMap
import com.rafaelboban.activitytracker.ui.components.map.hardlyVisible
import com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.ActivityBottomSheetContent
import com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.components.ActivityTabType
import com.rafaelboban.activitytracker.ui.screens.activity.components.ActivityTopAppBar
import com.rafaelboban.activitytracker.ui.screens.activity.components.HeartRateZoneIndicatorVertical
import com.rafaelboban.activitytracker.util.UserData
import com.rafaelboban.core.shared.model.ActivityStatus
import com.rafaelboban.core.shared.model.ActivityStatus.Companion.isActive
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.shared.model.HeartRatePoint
import com.rafaelboban.core.shared.ui.util.ObserveAsEvents
import com.rafaelboban.core.shared.utils.ActivityDataFormatter
import com.rafaelboban.core.shared.utils.ActivityDataFormatter.formatElapsedTimeDisplay
import com.rafaelboban.core.shared.utils.ActivityDataFormatter.roundToDecimals
import com.rafaelboban.core.shared.utils.DEFAULT_HEART_RATE_TRACKER_AGE
import com.rafaelboban.core.shared.utils.HeartRateZoneHelper
import com.rafaelboban.core.theme.R
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Montserrat
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import kotlin.time.Duration

@Composable
fun ActivityScreenRoot(
    navigateUp: () -> Boolean,
    viewModel: ActivityViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            ActivityEvent.NavigateBack -> navigateUp()
            ActivityEvent.OpenGoals -> {
                viewModel.onAction(ActivityAction.OnTabChanged(ActivityTabType.GOALS))
                scope.launch {
                    scaffoldState.bottomSheetState.expand()
                }
            }
        }
    }

    BackHandler(enabled = viewModel.state.status.isActive) {
        viewModel.onAction(ActivityAction.OnBackClick)
    }

    ActivityScreen(
        state = viewModel.state,
        scaffoldState = scaffoldState,
        toggleTrackerService = { shouldRun ->
            if (shouldRun) {
                context.startService(ActivityTrackerService.createStartIntent(context))
            } else {
                context.startService(ActivityTrackerService.createStopIntent(context))
            }
        },
        onAction = { action ->
            if (action == ActivityAction.OnBackClick) {
                if (viewModel.state.status.isActive.not()) {
                    navigateUp()
                }
            }

            viewModel.onAction(action)
        }
    )
}

@Composable
fun ActivityScreen(
    state: ActivityState,
    onAction: (ActivityAction) -> Unit,
    toggleTrackerService: (Boolean) -> Unit,
    scaffoldState: BottomSheetScaffoldState
) {
    val density = LocalDensity.current
    val navigationBarPadding = with(density) { WindowInsets.navigationBars.getBottom(density).toDp() }

    val scrollState = rememberScrollState()

    val canSwipeBottomSheet by remember {
        derivedStateOf {
            scrollState.value == 0 || scaffoldState.bottomSheetState.hasExpandedState.not()
        }
    }

    LaunchedEffect(state.status) {
        if (state.status == ActivityStatus.IN_PROGRESS && ActivityTrackerService.isActive.not()) {
            toggleTrackerService(true)
        }

        if (state.status == ActivityStatus.FINISHED && ActivityTrackerService.isActive) {
            toggleTrackerService(false)
        }
    }

    DialogScaffold(
        showDialog = state.showDiscardDialog || state.showSelectMapTypeDialog || state.showSetGoalsDialog,
        onDismiss = { onAction(ActivityAction.DismissDialogs) }
    ) {
        when {
            state.showDiscardDialog -> InfoDialog(
                title = stringResource(id = com.rafaelboban.activitytracker.R.string.discard_activity),
                subtitle = stringResource(id = com.rafaelboban.activitytracker.R.string.discard_activity_info),
                actionText = stringResource(id = com.rafaelboban.activitytracker.R.string.discard),
                actionButtonColor = MaterialTheme.colorScheme.error,
                actionButtonTextColor = MaterialTheme.colorScheme.onError,
                onDismissClick = { onAction(ActivityAction.DismissDialogs) },
                onActionClick = {
                    toggleTrackerService(false)
                    onAction(ActivityAction.DiscardActivity)
                }
            )

            state.showSelectMapTypeDialog -> {
                SelectMapTypeDialog(
                    currentType = state.mapType,
                    onDismissClick = { onAction(ActivityAction.DismissDialogs) },
                    onConfirmClick = { type -> onAction(ActivityAction.OnSelectMapType(type)) }
                )
            }

            state.showSetGoalsDialog -> {
                SetActivityGoalsDialog(
                    onActionClick = { onAction(ActivityAction.OpenGoals) },
                    onDismissClick = { doNotShowAgain ->
                        onAction(ActivityAction.DismissGoalsDialog(doNotShowAgain))
                    }
                )
            }
        }
    }

    BoxWithConstraints {
        val boxHeight = maxHeight

        BottomSheetScaffold(
            sheetSwipeEnabled = canSwipeBottomSheet,
            scaffoldState = scaffoldState,
            sheetPeekHeight = 36.dp + navigationBarPadding,
            sheetContainerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            sheetContent = {
                ActivityBottomSheetContent(
                    state = state,
                    scrollState = scrollState,
                    selectedTab = state.selectedBottomSheetTab,
                    onTabSelected = { tab -> onAction(ActivityAction.OnTabChanged(tab)) },
                    onLoadWeather = { onAction(ActivityAction.OnReloadWeather) },
                    modifier = Modifier.height(boxHeight * 0.6f)
                )
            }
        ) {
            ConstraintLayout(
                modifier = Modifier.fillMaxWidth()
            ) {
                val (infoCard, map, controls, heart, zoneIndicator, lockCameraButton, mapTypeButton) = createRefs()

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
                        activityType = state.type,
                        onBackClick = { onAction(ActivityAction.OnBackClick) },
                        gpsOk = if (state.status != ActivityStatus.FINISHED) state.currentLocation != null else null
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 12.dp)
                    ) {
                        ActivityDataColumn(
                            modifier = Modifier.weight(1f),
                            title = stringResource(id = com.rafaelboban.activitytracker.R.string.duration),
                            value = state.duration.formatElapsedTimeDisplay(),
                            icon = Icons.Outlined.Timer
                        )

                        VerticalDivider(modifier = Modifier.height(24.dp))

                        ActivityDataColumn(
                            modifier = Modifier.weight(1f),
                            title = stringResource(id = com.rafaelboban.activitytracker.R.string.distance),
                            value = ActivityDataFormatter.formatDistanceDisplay(state.activityData.distanceMeters),
                            unit = if (state.activityData.distanceMeters < 1000) "m" else "km",
                            icon = Icons.AutoMirrored.Outlined.TrendingUp
                        )

                        VerticalDivider(modifier = Modifier.height(24.dp))

                        if (state.type.showPace) {
                            ActivityDataColumn(
                                modifier = Modifier.weight(1.2f),
                                title = stringResource(id = com.rafaelboban.activitytracker.R.string.pace),
                                value = ActivityDataFormatter.convertSpeedToPace(state.activityData.speed),
                                unit = "min/km",
                                icon = Icons.Outlined.Speed
                            )
                        } else {
                            ActivityDataColumn(
                                modifier = Modifier.weight(1f),
                                title = stringResource(id = com.rafaelboban.activitytracker.R.string.speed),
                                value = state.activityData.speed.roundToDecimals(1),
                                unit = "km/h",
                                icon = Icons.Outlined.Speed
                            )
                        }
                    }
                }

                ActivityTrackerMap(
                    currentLocation = state.currentLocation,
                    locations = state.activityData.locations,
                    cameraLocked = state.mapCameraLocked,
                    mapType = state.mapType,
                    activityType = state.type,
                    maxSpeed = state.maxSpeed,
                    modifier = Modifier.constrainAs(map) {
                        top.linkTo(infoCard.bottom, margin = (-16).dp)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.matchParent
                        height = Dimension.fillToConstraints
                    }
                )

                if (state.status != ActivityStatus.FINISHED) {
                    AnimatedContent(
                        targetState = state.status,
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
                                        onClick = { onAction(ActivityAction.OnStartClick) },
                                        showBorder = state.mapType.hardlyVisible
                                    )
                                }
                            }

                            ActivityStatus.IN_PROGRESS -> {
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    ActivityFloatingActionButton(
                                        icon = Icons.Filled.Pause,
                                        onClick = { onAction(ActivityAction.OnPauseClick) },
                                        showBorder = state.mapType.hardlyVisible
                                    )
                                }
                            }

                            ActivityStatus.PAUSED -> {
                                Row {
                                    ActivityFloatingActionButton(
                                        icon = Icons.Filled.PlayArrow,
                                        onClick = { onAction(ActivityAction.OnResumeClick) },
                                        showBorder = state.mapType.hardlyVisible
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    ActivityFloatingActionButton(
                                        icon = ImageVector.vectorResource(id = R.drawable.ic_finish_flag),
                                        onClick = { onAction(ActivityAction.OnFinishClick) },
                                        showBorder = state.mapType.hardlyVisible
                                    )
                                }
                            }

                            else -> Unit
                        }
                    }
                }

                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 24.dp) {
                    val backgroundColor = if (state.mapType.hardlyVisible) MaterialTheme.colorScheme.background.copy(alpha = 0.8f) else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)

                    IconButton(
                        onClick = { onAction(ActivityAction.OnCameraLockToggle) },
                        modifier = Modifier
                            .background(shape = CircleShape, color = backgroundColor)
                            .constrainAs(lockCameraButton) {
                                end.linkTo(parent.end, margin = 4.dp)
                                top.linkTo(infoCard.bottom, margin = 12.dp)
                            }
                    ) {
                        Icon(
                            imageVector = if (state.mapCameraLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                            tint = MaterialTheme.colorScheme.tertiary,
                            contentDescription = null
                        )
                    }

                    IconButton(
                        onClick = { onAction(ActivityAction.OnOpenSelectMapType) },
                        modifier = Modifier
                            .background(shape = CircleShape, color = backgroundColor)
                            .constrainAs(mapTypeButton) {
                                end.linkTo(lockCameraButton.end)
                                top.linkTo(lockCameraButton.bottom, margin = 8.dp)
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Map,
                            tint = MaterialTheme.colorScheme.tertiary,
                            contentDescription = null
                        )
                    }
                }

                state.activityData.currentHeartRate
                    ?.takeIf { state.status.isActive }
                    ?.let { point ->
                        val zoneData = HeartRateZoneHelper.getHeartRateZone(point.heartRate, UserData.user?.age ?: DEFAULT_HEART_RATE_TRACKER_AGE)

                        HeartRateZoneIndicatorVertical(
                            currentZone = zoneData.zone,
                            ratioInZone = zoneData.ratioInZone,
                            modifier = Modifier.constrainAs(zoneIndicator) {
                                start.linkTo(parent.start)
                                top.linkTo(infoCard.bottom, margin = 8.dp)
                                bottom.linkTo(parent.bottom, margin = 44.dp)
                                height = Dimension.fillToConstraints
                            }
                        )

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.constrainAs(heart) {
                                top.linkTo(infoCard.bottom, margin = 8.dp)
                                start.linkTo(parent.start, margin = 12.dp)
                                width = Dimension.value(56.dp)
                            }
                        ) {
                            Icon(
                                modifier = Modifier.size(56.dp),
                                imageVector = Icons.Filled.Favorite,
                                tint = MaterialTheme.colorScheme.error,
                                contentDescription = null
                            )

                            Text(
                                text = point.heartRate.toString(),
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
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
            scaffoldState = rememberBottomSheetScaffoldState(),
            state = ActivityState(
                status = ActivityStatus.IN_PROGRESS,
                type = ActivityType.WALK,
                duration = Duration.parse("1h 30m 52s"),
                activityData = ActivityData(
                    distanceMeters = 1925,
                    speed = 9.2f,
                    heartRatePoints = persistentListOf(HeartRatePoint(102, Duration.ZERO)),
                    currentHeartRate = HeartRatePoint(102, Duration.ZERO)
                )
            )
        )
    }
}
