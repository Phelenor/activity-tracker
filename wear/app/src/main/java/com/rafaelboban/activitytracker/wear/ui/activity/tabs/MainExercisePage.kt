package com.rafaelboban.activitytracker.wear.ui.activity.tabs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.rafaelboban.activitytracker.wear.R
import com.rafaelboban.activitytracker.wear.ui.activity.ActivityAction
import com.rafaelboban.activitytracker.wear.ui.activity.ActivityState
import com.rafaelboban.activitytracker.wear.ui.components.ActivityActionButton
import com.rafaelboban.activitytracker.wear.ui.components.StatisticItem
import com.rafaelboban.core.shared.model.ActivityStatus
import com.rafaelboban.core.shared.utils.ActivityDataFormatter
import com.rafaelboban.core.shared.utils.ActivityDataFormatter.formatElapsedTimeDisplay
import com.rafaelboban.core.shared.utils.ActivityDataFormatter.roundToDecimals
import com.rafaelboban.core.theme.wear.ActivityTrackerWearTheme

@Composable
fun MainExercisePage(
    state: ActivityState,
    onAction: (ActivityAction) -> Unit
) {
    val isCircleShape = LocalConfiguration.current.isScreenRound
    val screenSize = LocalConfiguration.current.screenWidthDp

    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        val (time, controls, statsTop) = createRefs()

        Row(
            modifier = Modifier
                .padding(horizontal = if (isCircleShape) 24.dp else 8.dp)
                .constrainAs(statsTop) {
                    bottom.linkTo(time.top, margin = 8.dp)
                    width = Dimension.matchParent
                }
        ) {
            StatisticItem(
                value = ActivityDataFormatter.formatDistanceDisplay(state.distanceMeters),
                unit = if (state.distanceMeters < 1000) "m" else "km",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (state.activityType?.showPace == true) {
                StatisticItem(
                    value = ActivityDataFormatter.convertSpeedToPace(state.speed),
                    unit = "min/km",
                    icon = Icons.Default.Speed,
                    modifier = Modifier.weight(1.2f)
                )
            } else {
                StatisticItem(
                    value = state.speed.roundToDecimals(1),
                    unit = "km/h",
                    icon = Icons.Default.Speed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Text(
            text = state.duration.formatElapsedTimeDisplay(),
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.primary)
                .padding(vertical = 2.dp, horizontal = 4.dp)
                .constrainAs(time) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.matchParent
                }
        )

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
                .padding(top = if (screenSize > 192) 16.dp else 8.dp)
                .constrainAs(controls) {
                    top.linkTo(time.bottom)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.wrapContent
                    height = Dimension.fillToConstraints
                }
        ) { status ->
            when (status) {
                ActivityStatus.NOT_STARTED -> {
                    Box(contentAlignment = Alignment.TopCenter) {
                        if (state.isGroupActivity.not() || state.isGroupActivityOwner) {
                            ActivityActionButton(
                                icon = Icons.Filled.PlayArrow,
                                onClick = { onAction(ActivityAction.OnStartClick) }
                            )
                        } else {
                            ActivityStatusLabel(
                                label = stringResource(R.string.waiting)
                            )
                        }
                    }
                }

                ActivityStatus.IN_PROGRESS -> {
                    Box(contentAlignment = Alignment.TopCenter) {
                        if (state.isGroupActivity.not() || state.isGroupActivityOwner) {
                            ActivityActionButton(
                                icon = Icons.Filled.Pause,
                                onClick = { onAction(ActivityAction.OnPauseClick) }
                            )
                        } else {
                            ActivityStatusLabel(
                                label = stringResource(R.string.in_progress)
                            )
                        }
                    }
                }

                ActivityStatus.PAUSED -> {
                    Box(contentAlignment = Alignment.TopCenter) {
                        if (state.isGroupActivity.not() || state.isGroupActivityOwner) {
                            ActivityActionButton(
                                icon = Icons.Filled.PlayArrow,
                                onClick = { onAction(ActivityAction.OnResumeClick) }
                            )
                        } else {
                            ActivityStatusLabel(
                                label = stringResource(com.rafaelboban.core.theme.R.string.paused)
                            )
                        }
                    }
                }

                else -> Unit
            }
        }
    }
}

@Composable
fun ActivityStatusLabel(
    label: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onTertiary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .padding(top = 8.dp)
            .background(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.tertiary)
            .padding(vertical = 4.dp, horizontal = 8.dp)
    )
}

@WearPreviewDevices
@Composable
private fun MainExercisePagePreview() {
    ActivityTrackerWearTheme {
        MainExercisePage(
            onAction = {},
            state = ActivityState(
                activityStatus = ActivityStatus.IN_PROGRESS,
                isGroupActivity = true
            )
        )
    }
}
