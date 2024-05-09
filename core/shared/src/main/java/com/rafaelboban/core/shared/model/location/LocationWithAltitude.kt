package com.rafaelboban.core.shared.model.location

data class LocationWithAltitude(
    val location: Location,
    val altitude: Double,
    val speed: Float?
)
