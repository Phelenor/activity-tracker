package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.components.HeartRateZoneProgressBar
import com.rafaelboban.core.shared.utils.ActivityDataFormatter.convertSpeedToPace
import com.rafaelboban.core.shared.utils.ActivityDataFormatter.formatDistanceDisplay
import com.rafaelboban.core.shared.utils.ActivityDataFormatter.formatElapsedTimeDisplay
import com.rafaelboban.core.shared.utils.ActivityDataFormatter.roundToDecimals
import com.rafaelboban.core.shared.utils.HeartRateZone
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

@Composable
fun ActivityHeartZoneAnalysisCard(
    heartZoneDistribution: Map<HeartRateZone, Float>,
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
        Text(
            text = "Heart Rate Zones",
            style = Typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )

        HeartRateZone.entries.forEach { zone ->
            HeartRateZoneProgressBar(
                modifier = Modifier.padding(start = 2.dp),
                zone = zone,
                progress = heartZoneDistribution[zone] ?: 0f
            )
        }
    }
}

@Preview
@Composable
private fun ActivityDetailsCardPreview() {
    ActivityTrackerTheme {
        ActivityHeartZoneAnalysisCard(heartZoneDistribution = HeartRateZone.entries.associateWith { Random.nextFloat() })
    }
}
