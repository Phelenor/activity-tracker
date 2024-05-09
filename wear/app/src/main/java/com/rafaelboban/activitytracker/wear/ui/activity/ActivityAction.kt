package com.rafaelboban.activitytracker.wear.ui.activity

sealed interface ActivityAction {

    data object GrantBodySensorsPermission : ActivityAction
}
