package com.rafaelboban.activitytracker.ui.screens.gymActivity

import com.rafaelboban.activitytracker.model.ActivityData
import com.rafaelboban.activitytracker.model.gym.GymEquipment
import com.rafaelboban.activitytracker.model.network.FetchStatus
import com.rafaelboban.core.shared.model.ActivityStatus
import kotlin.time.Duration

data class GymActivityState(
    val activityData: ActivityData = ActivityData(),
    val duration: Duration = Duration.ZERO,
    val showDiscardDialog: Boolean = false,
    val showDoYouWantToSaveDialog: Boolean = false,
    val status: ActivityStatus = ActivityStatus.NOT_STARTED,
    val isSaving: Boolean = false,
    val gymEquipment: GymEquipment? = null,
    val gymEquipmentFetchStatus: FetchStatus = FetchStatus.IN_PROGRESS
)
