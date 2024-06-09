package com.rafaelboban.activitytracker.ui.screens.gymActivity

sealed interface GymActivityEvent {
    data object NavigateBack : GymActivityEvent
    data object ActivitySaveError : GymActivityEvent
}
