package com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafaelboban.activitytracker.network.model.weather.WeatherAlert
import com.rafaelboban.activitytracker.network.model.weather.WeatherData
import com.rafaelboban.activitytracker.network.model.weather.WeatherForecast
import com.rafaelboban.activitytracker.network.model.weather.WeatherInfo
import com.rafaelboban.activitytracker.ui.components.ButtonSecondary
import com.rafaelboban.activitytracker.ui.components.LoadingIndicator
import com.rafaelboban.activitytracker.ui.components.WeatherIcon
import com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.components.WeatherForecastRow
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography
import kotlin.math.roundToInt

@Composable
fun ActivityWeatherTab(
    weather: WeatherForecast?,
    isLoading: Boolean,
    onReloadClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        when {
            weather != null -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            WeatherIcon(
                                code = weather.current.info.first().icon,
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "${weather.current.temp.roundToInt()} \u00B0C",
                                style = Typography.displayLarge
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Text(
                                style = Typography.labelLarge,
                                text = buildAnnotatedString {
                                    append("Humidity: ")

                                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                                        append("${weather.current.humidity.roundToInt()}%")
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = weather.current.info.first().description.capitalize(),
                            style = Typography.labelLarge
                        )
                    }

                    HorizontalDivider()

                    Text(
                        text = "Forecast",
                        style = Typography.displayLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    weather.hourly.forEach { weatherForHour ->
                        WeatherForecastRow(weather = weatherForHour)
                    }
                }
            }

            isLoading.not() -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 8.dp)
                ) {
                    Text(
                        text = "Weather fetch failed. Please try again.",
                        style = Typography.labelLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(Modifier.height(16.dp))

                    ButtonSecondary(
                        text = "Retry",
                        onClick = onReloadClick
                    )
                }
            }

            else -> {
                LoadingIndicator(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActivityWeatherTabPreview() {
    val currentWeatherData = WeatherData(
        timestamp = 1715900699,
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

    val hourlyWeatherData = listOf(
        WeatherData(
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
        ),
        WeatherData(
            timestamp = 1715904000,
            temp = 15.8f,
            humidity = 88f,
            info = listOf(
                WeatherInfo(
                    id = 500,
                    main = "Rain",
                    description = "light rain",
                    icon = "10n"
                )
            )
        ),
        WeatherData(
            timestamp = 1715907600,
            temp = 15.46f,
            humidity = 91f,
            info = listOf(
                WeatherInfo(
                    id = 804,
                    main = "Clouds",
                    description = "overcast clouds",
                    icon = "04n"
                )
            )
        ),
        WeatherData(
            timestamp = 1715911200,
            temp = 15.17f,
            humidity = 94f,
            info = listOf(
                WeatherInfo(
                    id = 804,
                    main = "Clouds",
                    description = "overcast clouds",
                    icon = "04n"
                )
            )
        ),
        WeatherData(
            timestamp = 1715914800,
            temp = 14.79f,
            humidity = 97f,
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

    val weatherAlerts = listOf(
        WeatherAlert(
            issuer = "DHMZ Državni hidrometeorološki zavod",
            event = "Yellow thunderstorm warning",
            startTimestamp = 1715896861,
            endTimestamp = 1715914800,
            description = "Severe thundershowers possible locally. lightning risk 40-60 %"
        )
    )

    val weatherForecast = WeatherForecast(
        current = currentWeatherData,
        hourly = hourlyWeatherData,
        alerts = weatherAlerts
    )

    ActivityTrackerTheme {
        ActivityWeatherTab(
            weather = weatherForecast,
            isLoading = false,
            onReloadClick = {}
        )
    }
}
