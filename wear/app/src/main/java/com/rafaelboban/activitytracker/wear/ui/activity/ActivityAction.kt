package com.rafaelboban.activitytracker.wear.ui.activity

sealed interface ActivityAction {

    data object GrantBodySensorsPermission : ActivityAction
    data object OnStartClick : ActivityAction
    data object OnPauseClick : ActivityAction
    data object OnResumeClick : ActivityAction
    data object OnFinishClick : ActivityAction
    data object OpenAppOnPhone : ActivityAction
}
