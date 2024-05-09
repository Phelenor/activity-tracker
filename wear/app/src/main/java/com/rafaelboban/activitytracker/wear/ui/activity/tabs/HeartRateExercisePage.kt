package com.rafaelboban.activitytracker.wear.ui.activity.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.rafaelboban.activitytracker.wear.ui.activity.ActivityState
import com.rafaelboban.activitytracker.wear.ui.components.StatisticItem
import com.rafaelboban.core.shared.utils.ActivityDataFormatter.formatElapsedTimeDisplay
import com.rafaelboban.core.theme.wear.ActivityTrackerWearTheme
import kotlin.time.Duration

@Composable
fun HeartRateExercisePage(
    state: ActivityState
) {
    val isCircleShape = LocalConfiguration.current.isScreenRound
    val screenSize = LocalConfiguration.current.screenWidthDp

    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        val (time, heartRateTop) = createRefs()

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(horizontal = if (isCircleShape) 24.dp else 8.dp)
                .constrainAs(heartRateTop) {
                    bottom.linkTo(time.top, margin = 8.dp)
                    width = Dimension.matchParent
                }
        ) {
            StatisticItem(
                value = state.heartRate.toString(),
                unit = "bpm",
                icon = Icons.Default.Favorite
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
    }
}

@WearPreviewDevices
@Composable
private fun HeartRateExercisePagePreview() {
    ActivityTrackerWearTheme {
        HeartRateExercisePage(
            state = ActivityState(
                heartRate = 122,
                duration = Duration.parse("1h 20m 32s")
            )
        )
    }
}
