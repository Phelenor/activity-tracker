package com.rafaelboban.core.shared.model

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class HeartRatePoint(
    val heartRate: Int,
    val timestamp: Duration
)
