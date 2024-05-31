package com.rafaelboban.activitytracker.model.network

import com.rafaelboban.activitytracker.network.model.goals.ActivityGoal
import com.rafaelboban.activitytracker.network.model.goals.ActivityGoalProgress
import com.rafaelboban.activitytracker.network.model.goals.ActivityGoalType
import com.rafaelboban.activitytracker.network.model.goals.GoalValueComparisonType
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.shared.utils.HeartRateZone
import kotlinx.serialization.Serializable
import java.time.Instant
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Serializable
data class Activity(
    val id: String = "",
    val activityType: ActivityType,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val distanceMeters: Int,
    val durationSeconds: Long,
    val avgSpeedKmh: Float,
    val avgHeartRate: Int,
    val maxSpeedKmh: Float = 0f,
    val maxHeartRate: Int,
    val calories: Int,
    val elevation: Int,
    val weather: ActivityWeatherInfo?,
    val heartRateZoneDistribution: Map<HeartRateZone, Float>,
    val goals: List<ActivityGoalProgress>,
    val imageUrl: String? = null,
    val groupActivityId: String? = null
) {

    companion object {

        val MockModel = Activity(
            id = "test-id",
            activityType = ActivityType.RUN,
            durationSeconds = (10.minutes + 30.seconds).inWholeSeconds,
            startTimestamp = Instant.now().epochSecond - 3.hours.inWholeSeconds,
            distanceMeters = 2543,
            avgSpeedKmh = 15.6234f,
            elevation = 123,
            imageUrl = null,
            avgHeartRate = 120,
            heartRateZoneDistribution = HeartRateZone.entries.associateWith { Random.nextFloat() },
            calories = 120,
            goals = List(5) {
                ActivityGoalProgress(
                    currentValue = 1.2f,
                    goal = ActivityGoal(
                        type = ActivityGoalType.DISTANCE,
                        valueType = GoalValueComparisonType.GREATER,
                        value = 3.4f,
                        label = "distance"
                    )
                )
            },
            endTimestamp = Instant.now().epochSecond,
            maxHeartRate = 150,
            maxSpeedKmh = 5.5f,
            weather = ActivityWeatherInfo(
                temp = 16f,
                humidity = 84f,
                icon = "04n",
                description = "Overcast clouds"
            )
        )
    }
}

@Serializable
data class ActivityWeatherInfo(
    val temp: Float,
    val humidity: Float,
    val description: String,
    val icon: String
)
