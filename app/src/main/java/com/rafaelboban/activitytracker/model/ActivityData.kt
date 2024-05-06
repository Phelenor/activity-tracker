package com.rafaelboban.activitytracker.model

import com.rafaelboban.activitytracker.model.location.LocationTimestamp

data class ActivityData(
    val distanceMeters: Int = 0,
    val speed: Float = 0f,
    val locations: List<List<LocationTimestamp>> = emptyList(),
    val heartRates: List<Int> = emptyList()
)
