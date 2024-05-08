package com.rafaelboban.core.tracker.utils

import com.rafaelboban.core.tracker.model.location.LocationTimestamp
import kotlin.math.abs
import kotlin.math.roundToInt

val List<LocationTimestamp>.distanceMeters: Int
    get() = zipWithNext { location1, location2 ->
        location1.latLong.distanceTo(location2.latLong).takeIf { it > 0.2f } ?: 0f
    }.sum().roundToInt()

val List<List<LocationTimestamp>>.distanceSequenceMeters: Int
    get() = sumOf { locationSequence -> locationSequence.distanceMeters }

val List<LocationTimestamp>.currentSpeed: Float
    get() {
        if (size < 2) return 0f

        val lastMoreThanMeterAway = findLast {
            val distanceOk = it.latLong.distanceTo(last().latLong) > 1
            val intervalOk = abs(it.durationTimestamp.inWholeSeconds - last().durationTimestamp.inWholeSeconds) in 1..4
            distanceOk && intervalOk
        } ?: run {
            return 0f
        }

        val distance = last().latLong.distanceTo(lastMoreThanMeterAway.latLong)
        val interval = last().durationTimestamp - lastMoreThanMeterAway.durationTimestamp

        return distance / interval.inWholeSeconds * 3.6f
    }
