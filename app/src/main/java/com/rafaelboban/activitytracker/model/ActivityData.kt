package com.rafaelboban.activitytracker.model

import com.rafaelboban.activitytracker.model.location.LocationTimestamp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ActivityData(
    val distanceMeters: Int = 0,
    val speed: Float = 0f,
    val locations: ImmutableList<ImmutableList<LocationTimestamp>> = persistentListOf(),
    val heartRates: ImmutableList<Int> = persistentListOf()
)
