package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafaelboban.activitytracker.R
import com.rafaelboban.activitytracker.network.ws.ActivityMessage
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.shared.utils.ActivityDataFormatter
import com.rafaelboban.core.shared.utils.ActivityDataFormatter.roundToDecimals
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography

@Composable
fun TrackerMapInfoWindow(
    data: ActivityMessage.UserDataSnapshot,
    activityType: ActivityType,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .widthIn(max = 200.dp)
            .border(color = MaterialTheme.colorScheme.tertiary, shape = RoundedCornerShape(8.dp), width = 1.dp)
            .background(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserImage(
                imageUrl = data.userImageUrl,
                modifier = Modifier.size(32.dp)
            )

            Spacer(Modifier.width(12.dp))

            Text(
                text = data.userDisplayName,
                style = Typography.labelLarge,
                fontSize = 16.sp,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            val speedLabel = stringResource(if (activityType.showPace) R.string.pace else R.string.speed)
            val speedDisplay = if (activityType.showPace) "${ActivityDataFormatter.convertSpeedToPace(data.speed)} min/km" else "${data.speed.roundToDecimals(1)} km/h"

            Text(
                text = "$speedLabel:",
                style = Typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = speedDisplay,
                style = Typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (data.heartRate != 0) {
            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stringResource(R.string.heartrate)}:",
                    style = Typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "${data.heartRate} bpm",
                    style = Typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview
@Composable
private fun TrackerMapInfoWindowPreview() {
    ActivityTrackerTheme {
        TrackerMapInfoWindow(
            activityType = ActivityType.RUN,
            data = ActivityMessage.UserDataSnapshot(
                userId = "123",
                userDisplayName = "Rafael",
                userImageUrl = null,
                lat = 1f,
                long = 1f,
                distance = 3300,
                heartRate = 120,
                speed = 8f
            )
        )
    }
}
