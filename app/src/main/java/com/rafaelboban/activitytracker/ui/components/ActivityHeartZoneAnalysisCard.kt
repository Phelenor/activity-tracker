package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.components.HeartRateZoneProgressBar
import com.rafaelboban.core.shared.utils.HeartRateZone
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography
import kotlin.random.Random

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
