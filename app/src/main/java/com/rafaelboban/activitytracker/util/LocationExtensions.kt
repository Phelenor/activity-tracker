package com.rafaelboban.activitytracker.util

import com.rafaelboban.activitytracker.model.location.LocationTimestamp
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.days


//fun LocationTimestamp.distanceToHaversine(other: LocationTimestamp) = this.location.location.distanceToHaversine(other.location.location)
//
//val List<LocationTimestamp>.distanceMetersHaversine: Int
//    get() = zipWithNext { location1, location2 ->
//        location1.distanceToHaversine(location2)
//    }.sum().roundToInt()
//
//val List<List<LocationTimestamp>>.distanceSequenceMetersHaversine: Int
//    get() = sumOf { locationSequence -> locationSequence.distanceMetersHaversine }
//
//fun LocationTimestamp.toAndroidLocation(): Location {
//    val latLong = location.location
//
//    return Location("").apply {
//        latitude = latLong.latitude
//        longitude = latLong.longitude
//    }
//}
//
//fun com.rafaelboban.activitytracker.model.location.Location.toAndroidLocation(): Location {
//    val loc = this
//
//    return Location("").apply {
//        latitude = loc.latitude
//        longitude = loc.longitude
//    }
//}

val List<LocationTimestamp>.distanceMeters: Int
    get() = zipWithNext { location1, location2 ->
        location1.latLong.distanceTo(location2.latLong)
    }.sum().roundToInt()

val List<List<LocationTimestamp>>.distanceSequenceMeters: Int
    get() = sumOf { locationSequence -> locationSequence.distanceMeters }

val List<LocationTimestamp>.currentSpeed: Float
    get() {
        if (size < 2) return 0f

        val lastMoreThanMeterAway = findLast {
            val distanceOk = it.latLong.distanceTo(last().latLong) > 1
            val intervalOk = abs(it.durationTimestamp.inWholeSeconds - last().durationTimestamp.inWholeSeconds) < 10
            distanceOk && intervalOk
        } ?: run {
            return 0f
        }

        val distance = last().latLong.distanceTo(lastMoreThanMeterAway.latLong)
        val interval = last().durationTimestamp - lastMoreThanMeterAway.durationTimestamp

        val distanceKilometer = distance / 1000f
        val intervalHours = (interval.inWholeMilliseconds / 1000f) / 1.days.inWholeSeconds

        return distanceKilometer / intervalHours

    }
