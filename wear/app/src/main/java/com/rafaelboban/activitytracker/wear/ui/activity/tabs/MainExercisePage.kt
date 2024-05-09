package com.rafaelboban.activitytracker.wear.ui.activity.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.rafaelboban.core.theme.R
import com.rafaelboban.activitytracker.wear.ui.activity.ActivityState
import com.rafaelboban.activitytracker.wear.ui.components.ActivityActionButton
import com.rafaelboban.activitytracker.wear.ui.components.StatisticItem
import com.rafaelboban.core.theme.wear.ActivityTrackerWearTheme
import com.rafaelboban.core.tracker.utils.ActivityDataFormatter
import com.rafaelboban.core.tracker.utils.ActivityDataFormatter.formatElapsedTimeDisplay
import com.rafaelboban.core.tracker.utils.ActivityDataFormatter.roundToDecimals

@Composable
fun MainExercisePage(
    state: ActivityState
) {
    val isCircleShape = LocalConfiguration.current.isScreenRound
    val screenSize = LocalConfiguration.current.screenWidthDp

    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        val (time, statsBottom, statsTop) = createRefs()

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

            StatisticItem(
                value = state.speed.roundToDecimals(1),
                unit = "km/h",
                icon = Icons.Default.Speed,
                modifier = Modifier.weight(1f)
            )
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

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(top = if (screenSize > 192) 16.dp else 8.dp)
                .constrainAs(statsBottom) {
                    top.linkTo(time.bottom)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.matchParent
                    height = Dimension.fillToConstraints
                }
        ) {
            ActivityActionButton(icon = Icons.Filled.PlayArrow, onClick = { })
            Spacer(modifier = Modifier.width(8.dp))
            ActivityActionButton(icon = ImageVector.vectorResource(id = R.drawable.ic_finish_flag), onClick = { })
        }
    }
}

@WearPreviewDevices
@Composable
private fun MainExercisePagePreview() {
    ActivityTrackerWearTheme {
        MainExercisePage(
            state = ActivityState()
        )
    }
}
