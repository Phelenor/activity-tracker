package com.rafaelboban.activitytracker.util

import android.location.Location
import com.rafaelboban.activitytracker.model.location.LocationWithAltitude

fun Location.toLocationWithAltitude(): LocationWithAltitude {
    return LocationWithAltitude(
        location = com.rafaelboban.activitytracker.model.location.Location(this.latitude, this.longitude),
        altitude = altitude
    )
}
