package com.rafaelboban.activitytracker.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.rafaelboban.activitytracker.model.location.LocationTimestamp
import com.rafaelboban.core.shared.utils.HeartRateZone
import com.rafaelboban.core.shared.utils.color
import kotlin.math.abs

object PolylineColorHelper {

    fun locationsToColor(
        location1: LocationTimestamp,
        location2: LocationTimestamp,
        minSpeed: Float,
        maxSpeed: Float
    ): Color {
        val distanceMeters = location1.latLong.distanceTo(location2.latLong)
        val timeDiff = abs((location2.durationTimestamp - location1.durationTimestamp).inWholeSeconds)
        val speedKmh = (distanceMeters / timeDiff) * 3.6f

        return interpolateColor(
            speedKmh = speedKmh,
            minSpeed = minSpeed,
            maxSpeed = maxSpeed,
            colorStart = HeartRateZone.AEROBIC.color,
            colorMid = HeartRateZone.ANAEROBIC.color,
            colorEnd = HeartRateZone.VO2_MAX.color
        )
    }

    private fun interpolateColor(
        speedKmh: Float,
        minSpeed: Float,
        maxSpeed: Float,
        colorStart: Color,
        colorMid: Color,
        colorEnd: Color
    ): Color {
        val ratio = ((speedKmh - minSpeed) / (maxSpeed - minSpeed)).coerceIn(0.0f..1.0f)
        val colorInt = if (ratio <= 0.5) {
            ColorUtils.blendARGB(colorStart.toArgb(), colorMid.toArgb(), ratio / 0.5f)
        } else {
            ColorUtils.blendARGB(colorMid.toArgb(), colorEnd.toArgb(), (ratio - 0.5f) / 0.5f)
        }

        return Color(colorInt)
    }
}
