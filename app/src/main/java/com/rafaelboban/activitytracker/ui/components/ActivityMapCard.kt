package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rafaelboban.activitytracker.model.network.ActivityWeatherInfo
import com.rafaelboban.activitytracker.util.DateHelper
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography
import java.time.Instant
import kotlin.math.roundToInt

@Composable
fun ActivityMapCard(
    startTimestamp: Long,
    imageUrl: String?,
    weather: ActivityWeatherInfo?,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primary, width = 2.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = DateHelper.formatTimestampToDateTime(startTimestamp),
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

        imageUrl?.let {
            MapImage(imageUrl = imageUrl)
        }
    }
}

@Preview
@Composable
private fun ActivityMapCardPreview() {
    ActivityTrackerTheme {
        ActivityMapCard(
            imageUrl = "",
            startTimestamp = Instant.now().epochSecond,
            weather = ActivityWeatherInfo(
                temp = 16f,
                humidity = 84f,
                icon = "04n",
                description = "Overcast clouds"
            )
        )
    }
}
