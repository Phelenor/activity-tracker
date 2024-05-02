package com.rafaelboban.activitytracker.ui.screens.main.dashboard

import com.rafaelboban.activitytracker.model.ActivityType

sealed interface DashboardAction {
    data object DismissBottomSheet : DashboardAction
    data object OpenSelectActivityBottomSheet : DashboardAction
    data class StartIndividualActivity(val type: ActivityType) : DashboardAction
}
