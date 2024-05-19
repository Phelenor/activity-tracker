package com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.twotone.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rafaelboban.activitytracker.R
import com.rafaelboban.activitytracker.model.ActivityData
import com.rafaelboban.activitytracker.ui.screens.activity.ActivityState
import com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.components.ActivityDetailsRow
import com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.components.HeartRateZoneProgressBar
import com.rafaelboban.activitytracker.util.UserData
import com.rafaelboban.core.shared.model.ActivityStatus
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.shared.model.HeartRatePoint
import com.rafaelboban.core.shared.utils.DEFAULT_HEART_RATE_TRACKER_AGE
import com.rafaelboban.core.shared.utils.F
import com.rafaelboban.core.shared.utils.HeartRateZone
import com.rafaelboban.core.shared.utils.HeartRateZoneHelper
import com.rafaelboban.core.shared.utils.color
import com.rafaelboban.core.shared.utils.label
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.roundToInt
import kotlin.time.Duration

@Composable
fun ActivityHeartRateTab(state: ActivityState) {
    val heartRatePoints = state.activityData.heartRatePoints

    val currentHeartRate = state.activityData.currentHeartRate?.heartRate
    val maxHeartRate = heartRatePoints.maxOfOrNull { it.heartRate } ?: 0
    val averageHeartRate = if (heartRatePoints.isNotEmpty()) (heartRatePoints.sumOf { it.heartRate } / heartRatePoints.size.F).roundToInt() else 0

    if (heartRatePoints.isEmpty() || currentHeartRate == null) {
        NoHeartRateInfo()
    } else {
        val zoneData = HeartRateZoneHelper.getHeartRateZone(currentHeartRate, UserData.user?.age ?: DEFAULT_HEART_RATE_TRACKER_AGE)
        val zoneDistribution = HeartRateZoneHelper.calculateHeartRateZoneDistribution(heartRatePoints, UserData.user?.age ?: DEFAULT_HEART_RATE_TRACKER_AGE, state.duration)

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            listOfNotNull(
                arrayOf(R.string.current_heartrate, "$currentHeartRate bpm", Icons.Outlined.FavoriteBorder, MaterialTheme.colorScheme.error).takeIf { state.status != ActivityStatus.FINISHED },
                arrayOf(R.string.average_heartrate, "$averageHeartRate bpm", Icons.TwoTone.Favorite, MaterialTheme.colorScheme.error),
                arrayOf(R.string.max_heartrate, "$maxHeartRate bpm", Icons.Filled.Favorite, MaterialTheme.colorScheme.error)
            ).forEach { (labelRes, value, icon, tint) ->
                ActivityDetailsRow(
                    label = stringResource(id = labelRes as Int),
                    value = value as String,
                    icon = icon as ImageVector,
                    iconTint = tint as Color
                )
            }

            if (state.status == ActivityStatus.FINISHED) {
                Text(
                    text = "HR zone analysis:",
                    style = Typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 16.dp)
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Current HR Zone:",
                        style = Typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "${zoneData.zone.ordinal} (${zoneData.zone.label})",
                        style = Typography.labelLarge,
                        color = zoneData.zone.color,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            zoneDistribution?.let { distribution ->
                HeartRateZone.entries.forEach { zone ->
                    HeartRateZoneProgressBar(
                        zone = zone,
                        progress = distribution[zone] ?: 0f
                    )
                }
            }
        }
    }
}

@Composable
private fun NoHeartRateInfo(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 32.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "info",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Start the activity and connect your watch to see heart rate analysis.",
            textAlign = TextAlign.Center,
            style = Typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ActivityHeartRateTabPreview() {
    ActivityTrackerTheme {
        ActivityHeartRateTab(
            state = ActivityState(
                type = ActivityType.RUN,
                activityData = ActivityData(
                    heartRatePoints = persistentListOf(HeartRatePoint(23, Duration.ZERO)),
                    currentHeartRate = HeartRatePoint(120, Duration.ZERO)
                )
            )
        )
    }
}
