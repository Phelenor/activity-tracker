package com.rafaelboban.activitytracker.ui.screens.main.dashboard

import com.rafaelboban.core.shared.model.ActivityType

sealed interface DashboardAction {
    data object DismissBottomSheet : DashboardAction
    data object DismissRationaleDialog : DashboardAction
    data object RequestPermissions : DashboardAction
    data object OpenSelectActivityTypeIndividualBottomSheet : DashboardAction
    data object OpenJoinGroupActivityBottomSheet : DashboardAction
    data object OpenConfigureGroupActivityBottomSheet : DashboardAction
    data class StartIndividualActivity(val type: ActivityType) : DashboardAction
    data class CreateGroupActivity(val type: ActivityType, val estimatedStartTimestamp: Long?) : DashboardAction
}
