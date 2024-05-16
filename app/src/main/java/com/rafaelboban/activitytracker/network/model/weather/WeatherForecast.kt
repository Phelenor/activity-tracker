package com.rafaelboban.activitytracker.network.model.weather

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherForecast(
    val current: WeatherData,
    val hourly: List<WeatherData>,
    val alerts: List<WeatherAlert>
)

@Serializable
data class WeatherData(
    @SerialName("dt")
    val timestamp: Long,
    val temp: Float,
    val humidity: Float,
    @SerialName("weather")
    val info: List<WeatherInfo>
)

@Serializable
data class WeatherInfo(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

@Serializable
data class WeatherAlert(
    @SerialName("sender_name")
    val issuer: String,
    val event: String,
    @SerialName("start")
    val startTimestamp: Long,
    @SerialName("end")
    val endTimestamp: Long,
    val description: String
)
