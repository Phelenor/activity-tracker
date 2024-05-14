package com.rafaelboban.activitytracker.wear.ui.activity

import com.rafaelboban.core.shared.model.ActivityStatus
import com.rafaelboban.core.shared.model.ActivityType
import kotlin.time.Duration

data class ActivityState(
    val activityStatus: ActivityStatus = ActivityStatus.NOT_STARTED,
    val duration: Duration = Duration.ZERO,
    val speed: Float = 0f,
    val distanceMeters: Int = 0,
    val heartRate: Int = 0,
    val userAge: Int = 30,
    val totalCaloriesBurned: Int = 0,
    val canTrack: Boolean = false,
    val activityType: ActivityType? = null,
    val canTrackHeartRate: Boolean = false,
    val canTrackCalories: Boolean = false,
    val isConnectedPhoneNearby: Boolean = false,
    val isInAmbientMode: Boolean = false,
    val isBurnInProtectionRequired: Boolean = false
)
