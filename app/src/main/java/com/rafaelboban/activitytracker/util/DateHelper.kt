package com.rafaelboban.activitytracker.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateHelper {

    fun getYearsSince(timestamp: Long): Int {
        val now = LocalDate.now()
        val then = timestamp.secondsToLocalDate()
        return ChronoUnit.YEARS.between(then, now).toInt()
    }

    fun formatTimestampToTime(timestamp: Long): String {
        val instant = Instant.ofEpochSecond(timestamp)
        val dateTime = instant.atZone(ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return dateTime.format(formatter)
    }

    fun formatTimestampToDate(timestamp: Long): String {
        val instant = Instant.ofEpochSecond(timestamp)
        val dateTime = instant.atZone(ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm")
        return dateTime.format(formatter)
    }

    private fun Long.secondsToLocalDate() = Instant.ofEpochSecond(this).atZone(ZoneId.systemDefault()).toLocalDate()

    private fun Long.secondsToLocalDateTime() = Instant.ofEpochSecond(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
}
