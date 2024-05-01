package com.rafaelboban.activitytracker.ui.screens.main.dashboard

sealed interface DashboardAction {
    data object OnActivityStartClick : DashboardAction
}
