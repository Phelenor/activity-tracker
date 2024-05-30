package com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Elevator
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.twotone.Favorite
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.rafaelboban.activitytracker.R
import com.rafaelboban.activitytracker.model.ActivityData
import com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.components.ActivityDetailsRow
import com.rafaelboban.core.shared.model.ActivityStatus
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.shared.utils.ActivityDataFormatter.convertSpeedToPace
import com.rafaelboban.core.shared.utils.ActivityDataFormatter.formatDistanceDisplay
import com.rafaelboban.core.shared.utils.ActivityDataFormatter.formatElapsedTimeDisplay
import com.rafaelboban.core.shared.utils.ActivityDataFormatter.roundToDecimals
import com.rafaelboban.core.shared.utils.F
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import kotlin.math.roundToInt
import kotlin.time.Duration

@Composable
fun ActivityDetailsTab(
    data: ActivityData,
    duration: Duration,
    type: ActivityType,
    status: ActivityStatus
) {
    val heartRatePoints = data.heartRatePoints
    val maxHeartRate = heartRatePoints.maxOfOrNull { it.heartRate } ?: 0
    val averageHeartRate = if (heartRatePoints.isNotEmpty()) (heartRatePoints.sumOf { it.heartRate } / heartRatePoints.size.F).roundToInt() else 0
    val averageSpeed = (data.distanceMeters / ((duration.inWholeSeconds.F).takeIf { it > 0 } ?: 1f)) * 3.6f

    val distanceUnit = if (data.distanceMeters < 1000) "m" else "km"
    val elevationUnit = if (data.elevationGain < 1000) "m" else "km"

    val speedPaceData = if (type.showPace) {
        arrayOf(R.string.pace, "${convertSpeedToPace(data.speed)} min/km", Icons.Outlined.Speed, MaterialTheme.colorScheme.onBackground)
    } else {
        arrayOf(R.string.speed, "${data.speed.roundToDecimals(1)} km/h", Icons.Default.Speed, MaterialTheme.colorScheme.onBackground)
    }

    val averageSpeedPaceData = if (type.showPace) {
        arrayOf(R.string.average_pace, "${convertSpeedToPace(averageSpeed)} min/km", Icons.Outlined.Speed, MaterialTheme.colorScheme.onBackground)
    } else {
        arrayOf(R.string.average_speed, "${averageSpeed.roundToDecimals(1)} km/h", Icons.Default.Speed, MaterialTheme.colorScheme.onBackground)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        listOfNotNull(
            arrayOf(R.string.duration, duration.formatElapsedTimeDisplay(), Icons.Default.Timer, MaterialTheme.colorScheme.onBackground),
            arrayOf(R.string.distance, "${formatDistanceDisplay(data.distanceMeters)} $distanceUnit", Icons.AutoMirrored.Filled.TrendingUp, MaterialTheme.colorScheme.onBackground),
            speedPaceData.takeIf { status != ActivityStatus.FINISHED },
            averageSpeedPaceData,
            arrayOf(R.string.elevation_gain, "${formatDistanceDisplay(data.elevationGain)} $elevationUnit", Icons.Outlined.Elevator, MaterialTheme.colorScheme.onBackground),
            arrayOf(R.string.current_heartrate, "${data.currentHeartRate?.heartRate} bpm", Icons.Outlined.FavoriteBorder, MaterialTheme.colorScheme.error).takeIf { data.currentHeartRate != null && status != ActivityStatus.FINISHED },
            arrayOf(R.string.average_heartrate, "$averageHeartRate bpm", Icons.TwoTone.Favorite, MaterialTheme.colorScheme.error).takeIf { data.currentHeartRate != null },
            arrayOf(R.string.max_heartrate, "$maxHeartRate bpm", Icons.Filled.Favorite, MaterialTheme.colorScheme.error).takeIf { data.currentHeartRate != null },
            arrayOf(R.string.calories_estimated, "${data.caloriesBurned} kcal", Icons.Filled.LocalFireDepartment, MaterialTheme.colorScheme.error).takeIf { data.caloriesBurned != null }
        ).forEach { (labelRes, value, icon, tint) ->
            ActivityDetailsRow(
                label = stringResource(id = labelRes as Int),
                value = value as String,
                icon = icon as ImageVector,
                iconTint = tint as Color
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActivityDetailsTabPreview() {
    ActivityTrackerTheme {
        ActivityDetailsTab(
            type = ActivityType.RUN,
            status = ActivityStatus.IN_PROGRESS,
            data = ActivityData(),
            duration = Duration.ZERO
        )
    }
}
