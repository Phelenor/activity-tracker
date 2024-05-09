package com.rafaelboban.core.shared.model

import com.rafaelboban.core.shared.model.location.LocationTimestamp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ActivityData(
    val distanceMeters: Int = 0,
    val speed: Float = 0f,
    val locations: ImmutableList<ImmutableList<LocationTimestamp>> = persistentListOf(),
    val heartRates: ImmutableList<Int> = persistentListOf()
)
