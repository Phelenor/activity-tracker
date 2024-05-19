package com.rafaelboban.activitytracker.model

import com.rafaelboban.core.shared.model.HeartRatePoint
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ActivityData(
    val distanceMeters: Int = 0,
    val speed: Float = 0f,
    val elevationGain: Int = 0,
    val locations: ImmutableList<ImmutableList<com.rafaelboban.activitytracker.model.location.LocationTimestamp>> = persistentListOf(),
    val heartRatePoints: ImmutableList<HeartRatePoint> = persistentListOf(),
    val currentHeartRate: HeartRatePoint? = null,
    val caloriesBurned: Int? = null,
    val startTimestamp: Long? = null,
    val endTimestamp: Long? = null
)
