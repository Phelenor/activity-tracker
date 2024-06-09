package com.rafaelboban.activitytracker.ui.screens.gymActivity

import com.rafaelboban.activitytracker.model.gym.GymEquipment
import com.rafaelboban.activitytracker.model.network.FetchStatus
import com.rafaelboban.core.shared.model.ActivityStatus
import kotlin.time.Duration

data class GymActivityState(
    val activityData: GymActivityData = GymActivityData(),
    val status: ActivityStatus = ActivityStatus.NOT_STARTED,
    val duration: Duration = Duration.ZERO,
    val showDiscardDialog: Boolean = false,
    val showDoYouWantToSaveDialog: Boolean = false,
    val gymEquipment: GymEquipment? = null,
    val gymEquipmentFetchStatus: FetchStatus = FetchStatus.IN_PROGRESS,
    val isSaving: Boolean = false,
    val startTimestamp: Long = 0,
    val endTimestamp: Long = 0
)

data class GymActivityData(
    val duration: Duration = Duration.ZERO,
    val distanceMeters: Int = 0,
    val speed: Float = 0f,
    val avgSpeed: Float = 0f,
    val heartRate: Int = 0,
    val avgHeartRate: Int = 0,
    val maxHeartRate: Int = 0,
    val maxSpeed: Float = 0f,
    val calories: Int = 0,
    val elevationGain: Int = 0
)
