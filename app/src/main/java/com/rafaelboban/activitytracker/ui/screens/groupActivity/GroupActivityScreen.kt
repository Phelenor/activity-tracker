@file:OptIn(ExperimentalMaterial3Api::class)

package com.rafaelboban.activitytracker.ui.screens.groupActivity

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelboban.activitytracker.model.ActivityData
import com.rafaelboban.activitytracker.model.network.FetchStatus
import com.rafaelboban.activitytracker.service.ActivityTrackerService
import com.rafaelboban.activitytracker.ui.components.ActivityDataColumn
import com.rafaelboban.activitytracker.ui.components.ActivityFloatingActionButton
import com.rafaelboban.activitytracker.ui.components.ButtonSecondary
import com.rafaelboban.activitytracker.ui.components.DialogScaffold
import com.rafaelboban.activitytracker.ui.components.InfoDialog
import com.rafaelboban.activitytracker.ui.components.LoadingIndicator
import com.rafaelboban.activitytracker.ui.components.SelectMapTypeDialog
import com.rafaelboban.activitytracker.ui.components.ShareGroupActivityDialog
import com.rafaelboban.activitytracker.ui.components.map.ActivityTrackerMap
import com.rafaelboban.activitytracker.ui.components.map.hardlyVisible
import com.rafaelboban.activitytracker.ui.screens.activity.ActivityScreen
import com.rafaelboban.activitytracker.ui.screens.activity.ActivityState
import com.rafaelboban.activitytracker.ui.screens.activity.components.ActivityTopAppBar
import com.rafaelboban.activitytracker.ui.screens.activity.components.HeartRateZoneIndicatorVertical
import com.rafaelboban.activitytracker.ui.screens.groupActivity.bottomsheet.GroupActivityBottomSheetContent
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
import com.rafaelboban.core.theme.mobile.Typography
import kotlinx.collections.immutable.persistentListOf
import java.io.ByteArrayOutputStream
import kotlin.time.Duration

@Composable
fun GroupActivityScreenRoot(
    navigateUp: () -> Boolean,
    viewModel: GroupActivityViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scaffoldState = rememberBottomSheetScaffoldState()

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            GroupActivityEvent.NavigateBack -> navigateUp()
            GroupActivityEvent.ActivitySaveError -> Toast.makeText(context, context.getString(R.string.activity_save_error), Toast.LENGTH_LONG).show()
        }
    }

    BackHandler(enabled = viewModel.state.status.isActive) {
        viewModel.onAction(GroupActivityAction.OnBackClick)
    }

    GroupActivityScreen(
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
            if (action == GroupActivityAction.OnBackClick) {
                if (viewModel.state.status.isActive.not()) {
                    navigateUp()
                }
            }

            viewModel.onAction(action)
        }
    )
}

@Composable
fun GroupActivityScreen(
    state: GroupActivityState,
    onAction: (GroupActivityAction) -> Unit,
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
        showDialog = state.showDiscardDialog || state.showSelectMapTypeDialog || state.showDoYouWantToSaveDialog || state.showShareDialog,
        onDismiss = { onAction(GroupActivityAction.DismissDialogs) }
    ) {
        when {
            state.showDiscardDialog -> InfoDialog(
                title = stringResource(id = com.rafaelboban.activitytracker.R.string.discard_activity),
                subtitle = stringResource(id = com.rafaelboban.activitytracker.R.string.discard_activity_info),
                actionText = stringResource(id = com.rafaelboban.activitytracker.R.string.discard),
                actionButtonColor = MaterialTheme.colorScheme.error,
                actionButtonTextColor = MaterialTheme.colorScheme.onError,
                onDismissClick = { onAction(GroupActivityAction.DismissDialogs) },
                onActionClick = {
                    toggleTrackerService(false)
                    onAction(GroupActivityAction.DiscardActivity)
                }
            )

            state.showDoYouWantToSaveDialog -> InfoDialog(
                title = stringResource(id = com.rafaelboban.activitytracker.R.string.save_activity_question),
                subtitle = stringResource(id = com.rafaelboban.activitytracker.R.string.short_activity_info),
                actionText = stringResource(id = com.rafaelboban.activitytracker.R.string.save),
                actionButtonColor = MaterialTheme.colorScheme.primary,
                actionButtonTextColor = MaterialTheme.colorScheme.onPrimary,
                onDismissClick = { onAction(GroupActivityAction.DismissDialogs) },
                onActionClick = { onAction(GroupActivityAction.SaveActivity) }
            )

            state.showSelectMapTypeDialog -> {
                SelectMapTypeDialog(
                    currentType = state.mapType,
                    onDismissClick = { onAction(GroupActivityAction.DismissDialogs) },
                    onConfirmClick = { type -> onAction(GroupActivityAction.OnSelectMapType(type)) }
                )
            }

            state.showShareDialog -> {
                val activity = checkNotNull(state.groupActivity)
                ShareGroupActivityDialog(
                    inviteText = "Join my ${stringResource(activity.activityType.singularRes)}.",
                    joinCode = activity.joinCode,
                    onDismissClick = { onAction(GroupActivityAction.DismissDialogs) }
                )
            }
        }
    }

    BoxWithConstraints {
        val boxHeight = maxHeight

        Crossfade(
            targetState = state.groupActivityFetchStatus,
            animationSpec = tween(200)
        ) { fetchStatus ->
            when (fetchStatus) {
                FetchStatus.IN_PROGRESS -> LoadingIndicator(modifier = Modifier.fillMaxSize())
                FetchStatus.ERROR -> GroupActivityFetchError(onRetryClick = { onAction(GroupActivityAction.RetryGroupActivityFetch) })
                FetchStatus.SUCCESS -> {
                    val activityType = checkNotNull(state.groupActivity?.activityType)

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
                            GroupActivityBottomSheetContent(
                                state = state,
                                scrollState = scrollState,
                                selectedTab = state.selectedBottomSheetTab,
                                onTabSelected = { tab -> onAction(GroupActivityAction.OnTabChanged(tab)) },
                                onLoadWeather = { onAction(GroupActivityAction.OnReloadWeather) },
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
                                    activityType = activityType,
                                    onBackClick = { onAction(GroupActivityAction.OnBackClick) },
                                    gpsOk = if (state.status != ActivityStatus.FINISHED) state.currentLocation != null else null,
                                    showShareButton = true,
                                    onShareClick = { onAction(GroupActivityAction.OnShareClick) }
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

                                    if (activityType.showPace) {
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
                                activityType = activityType,
                                maxSpeed = state.maxSpeed,
                                triggerMapSnapshot = state.isSaving,
                                onSnapshot = { bitmap ->
                                    val stream = ByteArrayOutputStream().apply {
                                        use {
                                            bitmap.compress(
                                                Bitmap.CompressFormat.JPEG,
                                                80,
                                                it
                                            )
                                        }
                                    }

                                    onAction(GroupActivityAction.MapSnapshotDone(stream.toByteArray()))
                                },
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
                                            Box(contentAlignment = Alignment.Center) {
                                                ActivityFloatingActionButton(
                                                    icon = Icons.Filled.PlayArrow,
                                                    onClick = { onAction(GroupActivityAction.OnStartClick) },
                                                    showBorder = state.mapType.hardlyVisible
                                                )
                                            }
                                        }

                                        ActivityStatus.IN_PROGRESS -> {
                                            Box(contentAlignment = Alignment.Center) {
                                                ActivityFloatingActionButton(
                                                    icon = Icons.Filled.Pause,
                                                    onClick = { onAction(GroupActivityAction.OnPauseClick) },
                                                    showBorder = state.mapType.hardlyVisible
                                                )
                                            }
                                        }

                                        ActivityStatus.PAUSED -> {
                                            Row {
                                                ActivityFloatingActionButton(
                                                    icon = Icons.Filled.PlayArrow,
                                                    onClick = { onAction(GroupActivityAction.OnResumeClick) },
                                                    showBorder = state.mapType.hardlyVisible
                                                )

                                                Spacer(modifier = Modifier.width(8.dp))

                                                ActivityFloatingActionButton(
                                                    icon = ImageVector.vectorResource(id = R.drawable.ic_finish_flag),
                                                    onClick = { onAction(GroupActivityAction.OnFinishClick) },
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
                                    onClick = { onAction(GroupActivityAction.OnCameraLockToggle) },
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
                                    onClick = { onAction(GroupActivityAction.OnOpenSelectMapType) },
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
        }

        AnimatedVisibility(
            visible = state.isSaving,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            LoadingIndicator(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun GroupActivityFetchError(
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 32.dp)
    ) {
        ButtonSecondary(
            text = stringResource(R.string.retry),
            onClick = onRetryClick,
            containerColor = MaterialTheme.colorScheme.error,
            textColor = MaterialTheme.colorScheme.onError
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.network_error_occurred),
            textAlign = TextAlign.Center,
            style = Typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
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
