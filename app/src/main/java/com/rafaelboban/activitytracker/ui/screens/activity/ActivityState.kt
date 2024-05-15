package com.rafaelboban.activitytracker.ui.screens.activity

import com.google.maps.android.compose.MapType
import com.rafaelboban.activitytracker.model.ActivityData
import com.rafaelboban.activitytracker.model.location.Location
import com.rafaelboban.core.shared.model.ActivityStatus
import com.rafaelboban.core.shared.model.ActivityType
import kotlin.time.Duration

data class ActivityState(
    val activityData: ActivityData = ActivityData(),
    val duration: Duration = Duration.ZERO,
    val currentLocation: Location? = null,
    val showDiscardDialog: Boolean = false,
    val activityStatus: ActivityStatus = ActivityStatus.NOT_STARTED,
    val mapCameraLocked: Boolean = true,
    val showSelectMapTypeDialog: Boolean = false,
    val mapType: MapType = MapType.NORMAL,
    val activityType: ActivityType
)
