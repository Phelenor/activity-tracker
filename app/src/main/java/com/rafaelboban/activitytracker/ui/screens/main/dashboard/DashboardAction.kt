package com.rafaelboban.activitytracker.ui.screens.main.dashboard

import com.rafaelboban.core.shared.model.ActivityType

sealed interface DashboardAction {
    data object DismissBottomSheet : DashboardAction
    data object DismissRationaleDialog : DashboardAction
    data object RequestPermissions : DashboardAction
    data object OpenSelectActivityBottomSheet : DashboardAction
    data class StartIndividualActivity(val type: ActivityType) : DashboardAction
}
