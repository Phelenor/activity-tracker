package com.rafaelboban.activitytracker.ui.screens.activity

sealed interface ActivityEvent {

    data object NavigateBack : ActivityEvent
    data object OpenGoals : ActivityEvent
    data object ActivitySaveError : ActivityEvent
}
