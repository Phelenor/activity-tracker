package com.rafaelboban.core.shared.utils

import com.rafaelboban.core.shared.utils.ActivityDataFormatter.roundToDecimals
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

object ActivityDataFormatter {

    fun Duration.formatElapsedTimeDisplay(): String {
        val hours = "%02d".format(inWholeSeconds / 1.hours.inWholeSeconds)
        val minutes = "%02d".format((inWholeSeconds % 1.hours.inWholeSeconds) / 1.hours.inWholeMinutes)
        val seconds = "%02d".format((inWholeSeconds % 1.minutes.inWholeSeconds))

        return if (inWholeHours > 0) {
            "$hours:$minutes:$seconds"
        } else {
            "$minutes:$seconds"
        }
    }

    fun Float.roundToDecimals(decimals: Int = 2) = "%.${decimals}f".format(this)

    fun formatDistanceDisplay(meters: Int): String {
        return if (meters < 1000) {
            meters.toString()
        } else {
            (meters / 1000f).roundToDecimals(2)
        }
    }

    fun convertSpeedToPace(speed: Float): String {
        if (speed < 2f) return "-"

        val pace = 60f / speed
        val minutes = pace.toInt()
        val seconds = ((pace - minutes) * 60).roundToInt()

        val minutesDisplay = "%d".format(minutes)
        val secondsDisplay = "%02d".format(seconds)

        return "$minutesDisplay:$secondsDisplay"
    }
}
