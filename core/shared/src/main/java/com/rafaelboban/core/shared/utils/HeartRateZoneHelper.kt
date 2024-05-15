package com.rafaelboban.core.shared.utils

import androidx.compose.ui.graphics.Color
import com.rafaelboban.core.shared.model.HeartRatePoint
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.seconds

enum class HeartRateZone(val range: ClosedFloatingPointRange<Float>) {
    AT_REST(range = 0.0f..0.5f),
    WARM_UP(range = 0.5f..0.6f),
    FAT_BURN(range = 0.6f..0.7f),
    AEROBIC(range = 0.7f..0.8f),
    ANAEROBIC(range = 0.8f..0.9f),
    VO2_MAX(range = 0.9f..1f);

    companion object {
        val Trackable = entries.drop(1)
    }
}

object HeartRateZoneHelper {

    fun getHeartRateZone(heartRate: Int, userAge: Int): HeartRateZoneData {
        val maxHeartRate = 220 - userAge
        return calculateHeartRateZone(heartRate, maxHeartRate)
    }

    fun calculateHeartRateZoneDistribution(heartRates: List<HeartRatePoint>, userAge: Int, totalDuration: Duration): Map<HeartRateZone, Float>? {
        if (heartRates.size < 2) return null
        if ((heartRates.last().timestamp - heartRates.first().timestamp) < 20.seconds) return null

        val timeSpentPerZone = mutableMapOf<HeartRateZone, Duration>()

        heartRates.zipWithNext { currentSample, nextSample ->
            val zone = getHeartRateZone(currentSample.heartRate, userAge).zone
            val duration = nextSample.timestamp - currentSample.timestamp
            timeSpentPerZone[zone] = timeSpentPerZone.getOrDefault(zone, ZERO) + duration
        }

        val lastSample = heartRates.last()
        val lastZone = getHeartRateZone(lastSample.heartRate, userAge).zone
        val duration = totalDuration - lastSample.timestamp

        timeSpentPerZone[lastZone] = timeSpentPerZone.getOrDefault(lastZone, ZERO) + duration

        return timeSpentPerZone.map { (zone, timeSpent) ->
            zone to (timeSpent / totalDuration).F
        }.toMap()
    }

    private fun calculateHeartRateZone(heartRate: Int, maxHeartRate: Int): HeartRateZoneData {
        val ratio = heartRate.F / maxHeartRate.F
        val zone = HeartRateZone.entries.lastOrNull { zone -> ratio in zone.range } ?: HeartRateZone.AT_REST

        return HeartRateZoneData(
            zone = zone,
            ratioInZone = (ratio - zone.range.start) / (zone.range.endInclusive - zone.range.start)
        )
    }
}

data class HeartRateZoneData(
    val zone: HeartRateZone,
    val ratioInZone: Float
)

val HeartRateZone.color: Color
    get() = when (this) {
        HeartRateZone.AT_REST -> Color.White
        HeartRateZone.WARM_UP -> Color(0xffa7b8ab)
        HeartRateZone.FAT_BURN -> Color(0xff6bbdd1)
        HeartRateZone.AEROBIC -> Color(0xff30b88a)
        HeartRateZone.ANAEROBIC -> Color(0xffbfbc58)
        HeartRateZone.VO2_MAX -> Color(0xFFDD6987)
    }

val HeartRateZone.label: String
    get() = when (this) {
        HeartRateZone.AT_REST -> "At Rest"
        HeartRateZone.WARM_UP -> "Warm Up Zone"
        HeartRateZone.FAT_BURN -> "Fat Burn Zone"
        HeartRateZone.AEROBIC -> "Aerobic Zone"
        HeartRateZone.ANAEROBIC -> "Anaerobic Zone"
        HeartRateZone.VO2_MAX -> "VO2 Max Zone"
    }

val HeartRateZone.intensityLabel: String
    get() = when (this) {
        HeartRateZone.AT_REST -> "At Rest"
        HeartRateZone.WARM_UP -> "Very Light"
        HeartRateZone.FAT_BURN -> "Light"
        HeartRateZone.AEROBIC -> "Moderate"
        HeartRateZone.ANAEROBIC -> "Hard"
        HeartRateZone.VO2_MAX -> "Maximum"
    }

val HeartRateZone.index: Int
    get() = ordinal
