package com.rafaelboban.activitytracker.wear.ui.activity.tabs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.composables.ProgressIndicatorSegment
import com.google.android.horologist.composables.SegmentedProgressIndicator
import com.google.android.horologist.composables.SquareSegmentedProgressIndicator
import com.rafaelboban.activitytracker.wear.ui.activity.ActivityState
import com.rafaelboban.activitytracker.wear.ui.components.StatisticItem
import com.rafaelboban.core.shared.utils.ActivityDataFormatter.formatElapsedTimeDisplay
import com.rafaelboban.core.shared.utils.HeartRateZone
import com.rafaelboban.core.shared.utils.HeartRateZoneHelper
import com.rafaelboban.core.shared.utils.color
import com.rafaelboban.core.shared.utils.index
import com.rafaelboban.core.shared.utils.label
import com.rafaelboban.core.theme.wear.ActivityTrackerWearTheme
import kotlin.time.Duration

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun HeartRateExercisePage(
    state: ActivityState
) {
    val isCircleShape = LocalConfiguration.current.isScreenRound
    val screenSize = LocalConfiguration.current.screenWidthDp

    val (zone, ratio) = HeartRateZoneHelper.getHeartRateZone(state.heartRate, state.userAge)

    val zoneProgress by animateFloatAsState(
        targetValue = if (zone == HeartRateZone.AT_REST) 0f else ((zone.ordinal - 1) / 5f) + (ratio / 5),
        label = "zone_progress_animation"
    )

    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        val (time, heartRateTop, caloriesBottom) = createRefs()

        Row(
            modifier = Modifier
                .padding(horizontal = if (isCircleShape) 32.dp else 12.dp)
                .constrainAs(heartRateTop) {
                    bottom.linkTo(time.top, margin = if (isCircleShape) 6.dp else 10.dp)
                    width = Dimension.matchParent
                }
        ) {
            StatisticItem(
                modifier = Modifier.weight(1.3f),
                value = state.duration.formatElapsedTimeDisplay(),
                unit = "",
                icon = Icons.Outlined.Timer
            )

            Spacer(modifier = Modifier.width(4.dp))

            if (state.canTrackCalories) {
                StatisticItem(
                    modifier = Modifier.weight(1f),
                    value = state.totalCaloriesBurned.toString(),
                    unit = "kcal",
                    icon = Icons.Default.LocalFireDepartment
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(8.dp))
                .padding(vertical = 2.dp, horizontal = 4.dp)
                .constrainAs(time) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start, margin = 12.dp)
                    end.linkTo(parent.end, margin = 12.dp)
                    width = Dimension.fillToConstraints
                }
        ) {
            Icon(
                imageVector = Icons.Outlined.Favorite,
                tint = MaterialTheme.colorScheme.error,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = state.heartRate.toString(),
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = if (isCircleShape) 24.dp else 8.dp)
                .constrainAs(caloriesBottom) {
                    top.linkTo(time.bottom, margin = 8.dp)
                    width = Dimension.matchParent
                }
        ) {
            Text(
                text = "HR  ZONE ${zone.index}",
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                color = zone.color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "(${zone.label})",
                style = MaterialTheme.typography.bodyExtraSmall,
                textAlign = TextAlign.Center,
                color = zone.color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        val segments = HeartRateZone.Trackable.map { zone ->
            ProgressIndicatorSegment(
                weight = 1f,
                indicatorColor = zone.color,
                trackColor = Color.White.copy(alpha = 0.25f)
            )
        }

        if (isCircleShape) {
            SegmentedProgressIndicator(
                trackSegments = segments,
                progress = zoneProgress,
                startAngle = -225f,
                endAngle = 45f,
                paddingAngle = 2f,
                strokeWidth = 6.dp
            )
        } else {
            SquareSegmentedProgressIndicator(
                modifier = Modifier.scale(scaleX = -1f, scaleY = 1f),
                trackSegments = segments,
                progress = 0.5f,
                strokeWidth = 6.dp,
                paddingDp = 2.dp,
                cornerRadiusDp = 8.dp
            )
        }
    }
}

@WearPreviewDevices
@Composable
private fun HeartRateExercisePagePreview() {
    ActivityTrackerWearTheme {
        HeartRateExercisePage(
            state = ActivityState(
                heartRate = 160,
                duration = Duration.parse("1h 20m 32s"),
                canTrackCalories = true,
                totalCaloriesBurned = 121
            )
        )
    }
}
