package com.rafaelboban.activitytracker.ui.screens.activity

sealed interface ActivityAction {
    data object OnBackClick : ActivityAction
}
