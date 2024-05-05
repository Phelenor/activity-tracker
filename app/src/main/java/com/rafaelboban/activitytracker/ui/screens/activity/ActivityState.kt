package com.rafaelboban.activitytracker.ui.screens.activity

import com.rafaelboban.activitytracker.model.ActivityData
import com.rafaelboban.activitytracker.model.location.Location
import kotlin.time.Duration

data class ActivityState(
    val activityData: ActivityData = ActivityData(),
    val duration: Duration = Duration.ZERO,
    val isActive: Boolean = false,
    val isStarted: Boolean = false,
    val currentLocation: Location? = null,
    val isFinished: Boolean = false,
    val isSaving: Boolean = false
)
