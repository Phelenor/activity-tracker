package com.rafaelboban.core.shared.utils

import android.location.Location
import com.rafaelboban.core.shared.model.location.LocationWithAltitude

fun Location.toLocationWithAltitude(): LocationWithAltitude {
    return LocationWithAltitude(
        location = com.rafaelboban.core.shared.model.location.Location(this.latitude, this.longitude),
        altitude = altitude,
        speed = if (hasSpeed()) speed.times(3.6f).takeIf { it > 0.8f } ?: 0f else null
    )
}
