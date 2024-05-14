package com.rafaelboban.activitytracker.wear.tracker

import com.rafaelboban.core.shared.model.HeartRatePoint

data class HealthData(
    val heartRate: HeartRatePoint?,
    val calories: Int?
)
