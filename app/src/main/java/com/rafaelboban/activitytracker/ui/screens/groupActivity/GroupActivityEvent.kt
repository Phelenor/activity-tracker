package com.rafaelboban.activitytracker.ui.screens.groupActivity

sealed interface GroupActivityEvent {

    data object NavigateBack : GroupActivityEvent
    data object ActivitySaveError : GroupActivityEvent
}
