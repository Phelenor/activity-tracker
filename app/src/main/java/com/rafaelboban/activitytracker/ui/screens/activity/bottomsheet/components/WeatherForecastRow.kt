package com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafaelboban.activitytracker.network.model.weather.WeatherData
import com.rafaelboban.activitytracker.network.model.weather.WeatherInfo
import com.rafaelboban.activitytracker.ui.components.WeatherIcon
import com.rafaelboban.activitytracker.util.DateHelper
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography
import kotlin.math.roundToInt

@Composable
fun WeatherForecastRow(
    weather: WeatherData,
    modifier: Modifier = Modifier
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = DateHelper.formatTimestampToTime(weather.timestamp),
                style = Typography.displayMedium
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "${weather.temp.roundToInt()} \u00B0C",
                style = Typography.displayMedium
            )

            Spacer(modifier = Modifier.width(8.dp))

            WeatherIcon(
                code = weather.info.first().icon,
                modifier = Modifier
                    .size(32.dp)
                    .background(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
            )
        }

        HorizontalDivider()
    }
}

@Preview(showBackground = true)
@Composable
private fun WeatherForecastRowPreview() {
    ActivityTrackerTheme {
        WeatherForecastRow(
            weather = WeatherData(
                timestamp = 1715900400,
                temp = 16.06f,
                humidity = 85f,
                info = listOf(
                    WeatherInfo(
                        id = 804,
                        main = "Clouds",
                        description = "overcast clouds",
                        icon = "04n"
                    )
                )
            )
        )
    }
}
