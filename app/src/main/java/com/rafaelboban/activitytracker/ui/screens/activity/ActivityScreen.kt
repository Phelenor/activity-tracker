package com.rafaelboban.activitytracker.ui.screens.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.rafaelboban.activitytracker.ui.components.ActivityDataColumn
import com.rafaelboban.activitytracker.ui.components.ActivityFloatingActionButton
import com.rafaelboban.activitytracker.ui.components.ActivityTrackerMap
import com.rafaelboban.activitytracker.ui.screens.activity.components.ActivityTopAppBar
import com.rafaelboban.activitytracker.ui.theme.ActivityTrackerTheme
import com.rafaelboban.activitytracker.util.ActivityDataFormatter
import com.rafaelboban.activitytracker.util.ActivityDataFormatter.formatElapsedTimeDisplay
import com.rafaelboban.activitytracker.util.ActivityDataFormatter.roundToDecimals
import kotlin.time.Duration

@Composable
fun ActivityScreenRoot(
    navigateUp: () -> Boolean,
    viewModel: ActivityViewModel = hiltViewModel()
) {
    ActivityScreen(
        state = viewModel.state,
        onAction = { action ->
            when (action) {
                ActivityAction.OnBackClick -> navigateUp()
                else -> viewModel.onAction(action)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    state: ActivityState,
    onAction: (ActivityAction) -> Unit
) {
    val density = LocalDensity.current
    val navigationBarPadding = with(density) { WindowInsets.navigationBars.getBottom(density).toDp() }

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
                modifier = Modifier.constrainAs(map) {
                    top.linkTo(infoCard.bottom, margin = (-16).dp)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.matchParent
                    height = Dimension.fillToConstraints
                }
            )

            if (state.activityStatus != ActivityStatus.FINISHED) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 42.dp)
                        .zIndex(1f)
                        .constrainAs(controls) {
                            bottom.linkTo(parent.bottom)
                            width = Dimension.matchParent
                        }
                ) {
                    when (state.activityStatus) {
                        ActivityStatus.NOT_STARTED -> {
                            ActivityFloatingActionButton(
                                icon = Icons.Filled.PlayArrow,
                                onClick = { onAction(ActivityAction.OnStartClick) }
                            )
                        }

                        ActivityStatus.IN_PROGRESS -> {
                            ActivityFloatingActionButton(
                                icon = Icons.Filled.Pause,
                                onClick = { onAction(ActivityAction.OnPauseClick) }
                            )
                        }

                        ActivityStatus.PAUSED -> {
                            ActivityFloatingActionButton(
                                icon = Icons.Filled.PlayArrow,
                                onClick = { onAction(ActivityAction.OnResumeClick) }
                            )

                            ActivityFloatingActionButton(
                                icon = ImageVector.vectorResource(id = R.drawable.ic_finish_flag),
                                onClick = { onAction(ActivityAction.OnFinishClick) }
                            )
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
            state = ActivityState(
                duration = Duration.parse("1h 30m 52s"),
                activityData = ActivityData(distanceMeters = 1925, speed = 9.2f)
            )
        )
    }
}
