@file:OptIn(ExperimentalFoundationApi::class)

package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafaelboban.activitytracker.R
import com.rafaelboban.activitytracker.model.network.Activity
import com.rafaelboban.activitytracker.model.network.ActivityWeatherInfo
import com.rafaelboban.activitytracker.util.DateHelper
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.shared.utils.ActivityDataFormatter
import com.rafaelboban.core.shared.utils.ActivityDataFormatter.formatElapsedTimeDisplay
import com.rafaelboban.core.shared.utils.ActivityDataFormatter.roundToDecimals
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

@Composable
fun ActivityCard(
    activity: Activity,
    navigateToActivityOverview: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDropDown by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .border(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primary, width = 2.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .combinedClickable(
                    onClick = { navigateToActivityOverview() },
                    onLongClick = { showDropDown = true }
                )
                .padding(16.dp)
        ) {
            activity.imageUrl?.takeUnless { activity.isGymActivity || it.isBlank() }?.let {
                MapImage(imageUrl = activity.imageUrl)
            }

            ActivityDurationSection(
                duration = activity.durationSeconds,
                activityType = activity.activityType,
                isGroupActivity = activity.groupActivityId != null,
                isGymActivity = activity.isGymActivity,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            ActivityDateSection(
                timestamp = activity.startTimestamp,
                weather = activity.weather
            )

            DataGrid(
                activity = activity,
                modifier = Modifier.fillMaxWidth()
            )
        }

        DropdownMenu(
            expanded = showDropDown,
            onDismissRequest = { showDropDown = false }
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(id = R.string.delete),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = Typography.bodyMedium
                    )
                },
                onClick = {
                    showDropDown = false
                    onDeleteClick()
                }
            )
        }
    }
}

@Composable
fun MapImage(
    imageUrl: String?,
    modifier: Modifier = Modifier
) {
    ImageWithIndicator(
        url = imageUrl,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16 / 9f)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
    )
}

@Composable
private fun ActivityDurationSection(
    duration: Long,
    activityType: ActivityType,
    isGroupActivity: Boolean,
    isGymActivity: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color = MaterialTheme.colorScheme.tertiary, shape = CircleShape)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.tertiary,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(activityType.drawableRes),
                tint = MaterialTheme.colorScheme.onTertiary,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.duration),
                style = Typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = (1.seconds * duration.toInt()).formatElapsedTimeDisplay(),
                style = Typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (isGroupActivity) {
            Icon(
                imageVector = ImageVector.vectorResource(com.rafaelboban.core.shared.R.drawable.app_logo_main),
                tint = Color.Unspecified,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .scale(1.5f)
            )

            Spacer(modifier = Modifier.width(8.dp))
        } else if (isGymActivity) {
            Icon(
                painter = painterResource(R.drawable.ic_gym),
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
private fun ActivityDateSection(
    timestamp: Long,
    weather: ActivityWeatherInfo?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = DateHelper.formatTimestampToDateTime(timestamp),
            style = Typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.weight(1f))

        weather?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${weather.temp.roundToInt()} \u00B0C",
                    style = Typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(8.dp))

                WeatherIcon(
                    code = weather.icon,
                    modifier = Modifier
                        .size(32.dp)
                        .background(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
                )
            }
        }
    }
}

@Composable
private fun DataGrid(
    activity: Activity,
    modifier: Modifier = Modifier
) {
    val speedPaceItem = if (activity.activityType.showPace) {
        stringResource(id = R.string.avg_sign_pace) to ActivityDataFormatter.convertSpeedToPace(activity.avgSpeedKmh)
    } else {
        stringResource(id = R.string.avg_sign_speed) to activity.avgSpeedKmh.roundToDecimals(1)
    }

    val data = listOf(
        stringResource(id = R.string.distance) to ActivityDataFormatter.formatDistanceDisplay(activity.distanceMeters),
        speedPaceItem,
        if (activity.avgHeartRate > 0) {
            stringResource(R.string.avg_sign_heartrate) to activity.avgHeartRate.toString()
        } else {
            stringResource(id = R.string.elevation) to ActivityDataFormatter.formatDistanceDisplay(activity.elevation)
        }
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        data.forEachIndexed { index, (title, value) ->
            DataGridCell(
                title = title,
                value = value,
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp)
            )

            if (index != data.lastIndex) {
                VerticalDivider(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DataGridCell(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = Typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = value,
            style = Typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@PreviewLightDark
@Composable
private fun ActivityCardPreview() {
    ActivityTrackerTheme {
        ActivityCard(
            onDeleteClick = {},
            navigateToActivityOverview = {},
            activity = Activity.MockModel
        )
    }
}
