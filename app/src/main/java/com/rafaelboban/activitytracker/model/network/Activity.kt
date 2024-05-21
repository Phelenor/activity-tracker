package com.rafaelboban.activitytracker.model.network

import com.rafaelboban.activitytracker.network.model.goals.ActivityGoalProgress
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.shared.utils.HeartRateZone
import kotlinx.serialization.Serializable

@Serializable
data class Activity(
    val activityType: ActivityType,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val distanceMeters: Int,
    val durationSeconds: Long,
    val avgSpeedKmh: Float,
    val avgHeartRate: Int,
    val calories: Int,
    val elevation: Int,
    val weather: ActivityWeatherInfo?,
    val heartRateZoneDistribution: Map<HeartRateZone, Float>,
    val goals: List<ActivityGoalProgress>,
    val imageUrl: String? = null
)

@Serializable
data class ActivityWeatherInfo(
    val temp: Float,
    val humidity: Float,
    val description: String,
    val icon: String
)
