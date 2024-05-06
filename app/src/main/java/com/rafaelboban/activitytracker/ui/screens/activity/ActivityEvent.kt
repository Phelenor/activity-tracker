package com.rafaelboban.activitytracker.ui.screens.activity

sealed interface ActivityEvent {

    data object NavigateBack : ActivityEvent
}
