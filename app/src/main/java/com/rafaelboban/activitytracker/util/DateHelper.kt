package com.rafaelboban.activitytracker.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object DateHelper {

    fun getYearsSince(timestamp: Long): Int {
        val now = LocalDate.now()
        val then = timestamp.secondsToLocalDate()
        return ChronoUnit.YEARS.between(then, now).toInt()
    }

    private fun Long.secondsToLocalDate() = Instant.ofEpochSecond(this).atZone(ZoneId.systemDefault()).toLocalDate()

    private fun Long.secondsToLocalDateTime() = Instant.ofEpochSecond(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
}
