package com.rafaelboban.activitytracker.ui.screens.activity

import com.rafaelboban.core.shared.model.ActivityData
import com.rafaelboban.core.shared.model.ActivityStatus
import com.rafaelboban.core.shared.model.location.Location
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
