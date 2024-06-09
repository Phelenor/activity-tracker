@file:OptIn(ExperimentalMaterial3Api::class)


package com.rafaelboban.activitytracker.ui.screens.gymActivity

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelboban.activitytracker.model.ActivityData
import com.rafaelboban.activitytracker.model.gym.GymEquipment
import com.rafaelboban.activitytracker.model.network.FetchStatus
import com.rafaelboban.activitytracker.service.ActivityTrackerService
import com.rafaelboban.activitytracker.ui.components.ActivityDataColumn
import com.rafaelboban.activitytracker.ui.components.ActivityFloatingActionButton
import com.rafaelboban.activitytracker.ui.components.ButtonSecondary
import com.rafaelboban.activitytracker.ui.components.DialogScaffold
import com.rafaelboban.activitytracker.ui.components.InfoDialog
import com.rafaelboban.activitytracker.ui.components.LoadingIndicator
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
import com.rafaelboban.core.theme.mobile.ColorSuccess
import com.rafaelboban.core.theme.mobile.Montserrat
import com.rafaelboban.core.theme.mobile.Typography
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Duration

@Composable
fun GymActivityScreenRoot(
    navigateUp: () -> Boolean,
    viewModel: GymActivityViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            GymActivityEvent.NavigateBack -> navigateUp()
            GymActivityEvent.ActivitySaveError -> Toast.makeText(context, context.getString(R.string.activity_save_error), Toast.LENGTH_LONG).show()
        }
    }

    BackHandler(enabled = viewModel.state.status.isActive) {
        viewModel.onAction(GymActivityAction.OnBackClick)
    }

    GymActivityScreen(
        state = viewModel.state,
        toggleTrackerService = { shouldRun ->
            if (shouldRun) {
                context.startService(ActivityTrackerService.createStartIntent(context))
            } else {
                context.startService(ActivityTrackerService.createStopIntent(context))
            }
        },
        onAction = { action ->
            if (action == GymActivityAction.OnBackClick) {
                if (viewModel.state.status.isActive.not()) {
                    navigateUp()
                }
            }

            viewModel.onAction(action)
        }
    )
}

@Composable
fun GymActivityScreen(
    state: GymActivityState,
    onAction: (GymActivityAction) -> Unit,
    toggleTrackerService: (Boolean) -> Unit,
) {
    LaunchedEffect(state.status) {
        if (state.status == ActivityStatus.IN_PROGRESS && ActivityTrackerService.isActive.not()) {
            toggleTrackerService(true)
        }

        if (state.status == ActivityStatus.FINISHED && ActivityTrackerService.isActive) {
            toggleTrackerService(false)
        }
    }

    DialogScaffold(
        showDialog = state.showDiscardDialog || state.showDoYouWantToSaveDialog,
        onDismiss = { onAction(GymActivityAction.DismissDialogs) }
    ) {
        when {
            state.showDiscardDialog -> InfoDialog(
                title = stringResource(id = com.rafaelboban.activitytracker.R.string.discard_activity),
                subtitle = stringResource(id = com.rafaelboban.activitytracker.R.string.discard_activity_info),
                actionText = stringResource(id = com.rafaelboban.activitytracker.R.string.discard),
                actionButtonColor = MaterialTheme.colorScheme.error,
                actionButtonTextColor = MaterialTheme.colorScheme.onError,
                onDismissClick = { onAction(GymActivityAction.DismissDialogs) },
                onActionClick = {
                    toggleTrackerService(false)
                    onAction(GymActivityAction.DiscardActivity)
                }
            )

            state.showDoYouWantToSaveDialog -> InfoDialog(
                title = stringResource(id = com.rafaelboban.activitytracker.R.string.save_activity_question),
                subtitle = stringResource(id = com.rafaelboban.activitytracker.R.string.short_activity_info),
                actionText = stringResource(id = com.rafaelboban.activitytracker.R.string.save),
                actionButtonColor = MaterialTheme.colorScheme.primary,
                actionButtonTextColor = MaterialTheme.colorScheme.onPrimary,
                onDismissClick = { onAction(GymActivityAction.DismissDialogs) },
                onActionClick = { onAction(GymActivityAction.SaveActivity) }
            )
        }
    }

    Crossfade(
        targetState = state.gymEquipmentFetchStatus,
        animationSpec = tween(200)
    ) { fetchStatus ->
        when (fetchStatus) {
            FetchStatus.IN_PROGRESS -> LoadingIndicator(modifier = Modifier.fillMaxSize())
            FetchStatus.ERROR -> GroupActivityFetchError(onRetryClick = { onAction(GymActivityAction.RetryGymActivityFetch) })
            FetchStatus.SUCCESS -> {
                val activityType = checkNotNull(state.gymEquipment?.activityType)

                val gradient = Brush.radialGradient(
                    0.0f to MaterialTheme.colorScheme.tertiary,
                    1.0f to MaterialTheme.colorScheme.onTertiary,
                    radius = 1800f,
                    tileMode = TileMode.Clamp
                )

                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(gradient)
                ) {
                    val (infoCard, controls, heart, zoneIndicator, time, topData, bottomData) = createRefs()

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
                            onBackClick = { onAction(GymActivityAction.OnBackClick) },
                            gpsOk = null,
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            val blinkAnimation by rememberInfiniteTransition(label = "connection_blink").animateFloat(
                                initialValue = 0f,
                                targetValue = 1f,
                                label = "connection_blink",
                                animationSpec = infiniteRepeatable(
                                    animation = tween(durationMillis = 750),
                                    repeatMode = RepeatMode.Reverse
                                )
                            )

                            Text(
                                text = if (state.gymEquipment != null) "Connected to: ${state.gymEquipment.name}" else "Connecting...",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = Typography.displaySmall
                            )

                            if (state.gymEquipment != null) {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .size(8.dp)
                                        .background(shape = CircleShape, color = ColorSuccess)
                                        .alpha(blinkAnimation)
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .constrainAs(topData) {
                                width = Dimension.matchParent
                                bottom.linkTo(time.top, margin = 24.dp)
                            }
                    ) {
                        ActivityDataColumn(
                            title = stringResource(id = com.rafaelboban.activitytracker.R.string.distance),
                            value = ActivityDataFormatter.formatDistanceDisplay(state.activityData.distanceMeters),
                            unit = if (state.activityData.distanceMeters < 1000) "m" else "km",
                            icon = Icons.AutoMirrored.Outlined.TrendingUp,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .background(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surface)
                                .border(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primary, width = 1.dp)
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                        )


                        if (activityType.showPace) {
                            ActivityDataColumn(
                                title = stringResource(id = com.rafaelboban.activitytracker.R.string.pace),
                                value = ActivityDataFormatter.convertSpeedToPace(state.activityData.speed),
                                unit = "min/km",
                                icon = Icons.Outlined.Speed,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .background(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surface)
                                    .border(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primary, width = 1.dp)
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            )
                        } else {
                            ActivityDataColumn(
                                title = stringResource(id = com.rafaelboban.activitytracker.R.string.speed),
                                value = state.activityData.speed.roundToDecimals(1),
                                unit = "km/h",
                                icon = Icons.Outlined.Speed,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .background(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surface)
                                    .border(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primary, width = 1.dp)
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.primary)
                            .padding(vertical = 2.dp, horizontal = 4.dp)
                            .constrainAs(time) {
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                                width = Dimension.matchParent
                            }
                    ) {
                        Text(
                            text = state.duration.formatElapsedTimeDisplay(),
                            style = MaterialTheme.typography.displayLarge,
                            fontSize = 32.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .constrainAs(bottomData) {
                                width = Dimension.matchParent
                                top.linkTo(time.bottom, margin = 24.dp)
                            }
                    ) {
                        ActivityDataColumn(
                            title = stringResource(id = com.rafaelboban.activitytracker.R.string.avg_sign_distance),
                            value = ActivityDataFormatter.formatDistanceDisplay(state.activityData.distanceMeters),
                            unit = if (state.activityData.distanceMeters < 1000) "m" else "km",
                            icon = Icons.AutoMirrored.Outlined.TrendingUp,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .background(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surface)
                                .border(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primary, width = 1.dp)
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                        )


                        if (activityType.showPace) {
                            ActivityDataColumn(
                                title = stringResource(id = com.rafaelboban.activitytracker.R.string.avg_sign_pace),
                                value = ActivityDataFormatter.convertSpeedToPace(state.activityData.speed),
                                unit = "min/km",
                                icon = Icons.Outlined.Speed,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .background(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surface)
                                    .border(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primary, width = 1.dp)
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            )
                        } else {
                            ActivityDataColumn(
                                title = stringResource(id = com.rafaelboban.activitytracker.R.string.avg_sign_speed),
                                value = state.activityData.speed.roundToDecimals(1),
                                unit = "km/h",
                                icon = Icons.Outlined.Speed,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .background(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surface)
                                    .border(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primary, width = 1.dp)
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            )
                        }
                    }

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
                            .padding(bottom = 16.dp)
                            .zIndex(1f)
                            .constrainAs(controls) {
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                width = Dimension.value(250.dp)
                            }
                    ) { status ->
                        when (status) {
                            ActivityStatus.NOT_STARTED -> {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.heightIn(min = 72.dp)
                                ) {
                                    ActivityFloatingActionButton(
                                        icon = Icons.Filled.PlayArrow,
                                        onClick = { onAction(GymActivityAction.OnStartClick) }
                                    )
                                }
                            }

                            ActivityStatus.IN_PROGRESS -> {
                                Box(contentAlignment = Alignment.Center) {
                                    ActivityFloatingActionButton(
                                        icon = Icons.Filled.Pause,
                                        onClick = { onAction(GymActivityAction.OnPauseClick) },
                                    )
                                }
                            }

                            ActivityStatus.PAUSED -> {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ActivityFloatingActionButton(
                                        icon = Icons.Filled.PlayArrow,
                                        onClick = { onAction(GymActivityAction.OnResumeClick) },
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    ActivityFloatingActionButton(
                                        icon = ImageVector.vectorResource(id = R.drawable.ic_finish_flag),
                                        onClick = { onAction(GymActivityAction.OnFinishClick) },
                                    )
                                }
                            }

                            ActivityStatus.FINISHED -> {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.heightIn(min = 72.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.activity_finished),
                                        style = Typography.displayMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onTertiary,
                                        modifier = Modifier
                                            .background(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.tertiary)
                                            .padding(vertical = 4.dp, horizontal = 8.dp)
                                    )
                                }
                            }
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
                                    bottom.linkTo(parent.bottom, margin = 24.dp)
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
private fun GymActivityScreenPreview() {
    ActivityTrackerTheme {
        GymActivityScreen(
            onAction = {},
            toggleTrackerService = {},
            state = GymActivityState(
                status = ActivityStatus.IN_PROGRESS,
                gymEquipment = GymEquipment(
                    id = "safafas",
                    name = "Trake",
                    description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris luctus sapien sagittis dolor imperdiet maximus vel nec eros. Vivamus imperdiet velit dui, a tempor orci iaculis eget. Morbi fringilla interdum odio, ac tempus elit congue vel. Ut vitae bibendum ante. Praesent feugiat, urna vitae fringilla congue, tortor nunc lacinia magna, in commodo sapien libero eget sapien. Lorem ipsum dolor sit amet, consectetur adipiscing elit. ",
                    imageUrl = "test",
                    videoUrl = "sagas",
                    activityType = ActivityType.RUN
                ),
                gymEquipmentFetchStatus = FetchStatus.SUCCESS,
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
