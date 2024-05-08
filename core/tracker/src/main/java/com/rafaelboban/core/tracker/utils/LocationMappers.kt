package com.rafaelboban.core.tracker.utils

import android.location.Location
import com.rafaelboban.core.tracker.model.location.LocationWithAltitude

fun Location.toLocationWithAltitude(): LocationWithAltitude {
    return LocationWithAltitude(
        location = com.rafaelboban.core.tracker.model.location.Location(this.latitude, this.longitude),
        altitude = altitude,
        speed = if (hasSpeed()) speed.times(3.6f).takeIf { it > 0.8f } ?: 0f else null
    )
}
