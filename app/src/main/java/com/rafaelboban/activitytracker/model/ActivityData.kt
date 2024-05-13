package com.rafaelboban.activitytracker.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ActivityData(
    val distanceMeters: Int = 0,
    val speed: Float = 0f,
    val elevationGain: Int = 0,
    val locations: ImmutableList<ImmutableList<com.rafaelboban.activitytracker.model.location.LocationTimestamp>> = persistentListOf(),
    val heartRates: ImmutableList<Int> = persistentListOf()
)
