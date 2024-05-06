package com.rafaelboban.activitytracker.ui.screens.activity

import com.rafaelboban.activitytracker.model.ActivityData
import com.rafaelboban.activitytracker.model.location.Location
import kotlin.time.Duration

data class ActivityState(
    val activityData: ActivityData = ActivityData(),
    val duration: Duration = Duration.ZERO,
    val isActive: Boolean = false,
    val currentLocation: Location? = null,
    val isSaving: Boolean = false,
    val showDiscardDialog: Boolean = false,
    val activityStatus: ActivityStatus = ActivityStatus.NOT_STARTED
)

enum class ActivityStatus {
    NOT_STARTED, IN_PROGRESS, PAUSED, FINISHED;

    companion object {
        val ActivityStatus.isRunning: Boolean
            get() = this == IN_PROGRESS || this == PAUSED
    }
}
