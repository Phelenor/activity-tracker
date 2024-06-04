package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rafaelboban.activitytracker.R
import com.rafaelboban.activitytracker.model.ui.ActivityUserStatus
import com.rafaelboban.activitytracker.network.ws.ActivityMessage
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.shared.utils.ActivityDataFormatter
import com.rafaelboban.core.shared.utils.ActivityDataFormatter.formatElapsedTimeDisplay
import com.rafaelboban.core.shared.utils.ActivityDataFormatter.roundToDecimals
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.ColorSuccess
import com.rafaelboban.core.theme.mobile.Typography
import kotlin.time.Duration

@Composable
fun ActivityUserRow(
    data: ActivityMessage.UserDataSnapshot,
    status: ActivityUserStatus,
    activityType: ActivityType,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                UserImage(
                    modifier = Modifier.size(40.dp),
                    imageUrl = data.userImageUrl
                )

                Spacer(Modifier.width(16.dp))

                Text(
                    text = data.userDisplayName,
                    style = Typography.displayMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (status == ActivityUserStatus.Active || status == ActivityUserStatus.Connected) {
                    Box(
                        Modifier
                            .padding(start = 4.dp)
                            .size(8.dp)
                            .background(shape = CircleShape, color = ColorSuccess)
                    )
                }
            }

            when (status) {
                ActivityUserStatus.Joined, ActivityUserStatus.Connected -> {
                    Text(
                        text = "Scheduled",
                        style = Typography.displaySmall,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                ActivityUserStatus.Active -> {
                    val speedDisplay = if (activityType.showPace) "${ActivityDataFormatter.convertSpeedToPace(data.speed)} min/km" else "${data.speed.roundToDecimals(1)} km/h"

                    Icon(
                        imageVector = Icons.Outlined.Speed,
                        tint = MaterialTheme.colorScheme.tertiary,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = speedDisplay,
                        style = MaterialTheme.typography.displaySmall,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (data.heartRate != 0) {
                        VerticalDivider(
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .height(24.dp)
                                .padding(horizontal = 8.dp)
                        )

                        Icon(
                            imageVector = Icons.Outlined.Favorite,
                            tint = MaterialTheme.colorScheme.error,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = data.heartRate.toString(),
                            style = MaterialTheme.typography.displaySmall,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                is ActivityUserStatus.Finished -> {
                    Text(
                        text = stringResource(R.string.finished),
                        style = Typography.labelLarge,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    VerticalDivider(
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .height(24.dp)
                            .padding(horizontal = 8.dp)
                    )

                    Text(
                        text = status.duration.formatElapsedTimeDisplay(),
                        style = Typography.labelLarge,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        HorizontalDivider()
    }
}

@Preview(showBackground = true)
@Composable
private fun ActivityUserRowPreview() {
    ActivityTrackerTheme {
        ActivityUserRow(
            status = ActivityUserStatus.Finished(Duration.parse("1h 20m")),
//            status = ActivityUserStatus.Connected,
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
