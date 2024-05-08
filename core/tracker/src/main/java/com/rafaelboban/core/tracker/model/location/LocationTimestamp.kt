package com.rafaelboban.core.tracker.model.location

import android.location.Location
import kotlin.time.Duration

data class LocationTimestamp(
    val location: LocationWithAltitude,
    val durationTimestamp: Duration
) {

    val latLong: Location
        get() = Location("").apply {
            latitude = location.location.latitude
            longitude = location.location.longitude
        }
}
