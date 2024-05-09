package com.rafaelboban.activitytracker.wear.ui.activity

import com.rafaelboban.core.tracker.model.ActivityStatus
import kotlin.time.Duration

data class ActivityState(
    val duration: Duration = Duration.ZERO,
    val speed: Float = 0f,
    val distanceMeters: Int = 0,
    val heartRate: Int = 0,
    val isTrackable: Boolean = false,
    val activityStatus: ActivityStatus = ActivityStatus.NOT_STARTED,
    val isActive: Boolean = false,
    val canTrackHeartRate: Boolean = false,
    val isConnectedPhoneNearby: Boolean = false
)
