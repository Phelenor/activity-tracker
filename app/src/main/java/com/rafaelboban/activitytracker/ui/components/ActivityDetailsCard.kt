package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Elevator
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.twotone.Favorite
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rafaelboban.activitytracker.R
import com.rafaelboban.activitytracker.model.network.Activity
import com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.components.ActivityDetailsRow
import com.rafaelboban.core.shared.utils.ActivityDataFormatter.convertSpeedToPace
import com.rafaelboban.core.shared.utils.ActivityDataFormatter.formatDistanceDisplay
import com.rafaelboban.core.shared.utils.ActivityDataFormatter.formatElapsedTimeDisplay
import com.rafaelboban.core.shared.utils.ActivityDataFormatter.roundToDecimals
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography
import kotlin.time.Duration.Companion.seconds

@Composable
fun ActivityDetailsCard(
    activity: Activity,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primary, width = 2.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp)
    ) {
        val distanceUnit = if (activity.distanceMeters < 1000) "m" else "km"
        val elevationUnit = if (activity.elevation < 1000) "m" else "km"

        val maxSpeedPaceData = if (activity.activityType.showPace) {
            arrayOf(R.string.max_pace, "${convertSpeedToPace(activity.maxSpeedKmh)} min/km", Icons.Outlined.Speed, MaterialTheme.colorScheme.onBackground)
        } else {
            arrayOf(R.string.max_speed, "${activity.maxSpeedKmh.roundToDecimals(1)} km/h", Icons.Default.Speed, MaterialTheme.colorScheme.onBackground)
        }

        val averageSpeedPaceData = if (activity.activityType.showPace) {
            arrayOf(R.string.average_pace, "${convertSpeedToPace(activity.avgSpeedKmh)} min/km", Icons.Outlined.Speed, MaterialTheme.colorScheme.onBackground)
        } else {
            arrayOf(R.string.average_speed, "${activity.avgSpeedKmh.roundToDecimals(1)} km/h", Icons.Default.Speed, MaterialTheme.colorScheme.onBackground)
        }

        val durationFormatted = (1.seconds * activity.durationSeconds.toInt()).formatElapsedTimeDisplay()

        Text(
            text = stringResource(R.string.details),
            style = Typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )

        listOfNotNull(
            arrayOf(R.string.duration, durationFormatted, Icons.Default.Timer, MaterialTheme.colorScheme.onBackground),
            arrayOf(R.string.distance, "${formatDistanceDisplay(activity.distanceMeters)} $distanceUnit", Icons.AutoMirrored.Filled.TrendingUp, MaterialTheme.colorScheme.onBackground),
            maxSpeedPaceData,
            averageSpeedPaceData,
            arrayOf(R.string.elevation_gain, "${formatDistanceDisplay(activity.elevation)} $elevationUnit", Icons.Outlined.Elevator, MaterialTheme.colorScheme.onBackground),
            arrayOf(R.string.average_heartrate, "${activity.avgHeartRate} bpm", Icons.TwoTone.Favorite, MaterialTheme.colorScheme.error).takeIf { activity.avgHeartRate > 0 },
            arrayOf(R.string.max_heartrate, "${activity.maxHeartRate} bpm", Icons.Filled.Favorite, MaterialTheme.colorScheme.error).takeIf { activity.maxHeartRate > 0 },
            arrayOf(R.string.calories_estimated, "${activity.calories} kcal", Icons.Filled.LocalFireDepartment, MaterialTheme.colorScheme.error).takeIf { activity.calories > 0 }
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

@Preview
@Composable
private fun ActivityDetailsCardPreview() {
    ActivityTrackerTheme {
        ActivityDetailsCard(activity = Activity.MockModel)
    }
}
