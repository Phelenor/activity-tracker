package com.rafaelboban.core.tracker.model.location

data class LocationWithAltitude(
    val location: Location,
    val altitude: Double,
    val speed: Float?
)
