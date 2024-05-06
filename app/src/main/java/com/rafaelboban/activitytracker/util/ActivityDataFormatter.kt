package com.rafaelboban.activitytracker.util

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
}
