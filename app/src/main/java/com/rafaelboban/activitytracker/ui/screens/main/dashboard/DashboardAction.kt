package com.rafaelboban.activitytracker.ui.screens.main.dashboard

import com.rafaelboban.core.shared.model.ActivityType

sealed interface DashboardAction {
    data object DismissBottomSheet : DashboardAction
    data object DismissRationaleDialog : DashboardAction
    data object RequestPermissions : DashboardAction
    data object OpenSelectActivityTypeIndividualBottomSheet : DashboardAction
    data object OpenJoinGroupActivityBottomSheet : DashboardAction
    data object OpenQRCodeScanner : DashboardAction
    data object OpenConfigureGroupActivityBottomSheet : DashboardAction
    data object Refresh : DashboardAction
    data class JoinGroupActivity(val joinCode: String) : DashboardAction
    data class StartIndividualActivity(val type: ActivityType) : DashboardAction
    data class CreateGroupActivity(val type: ActivityType, val estimatedStartTimestamp: Long?) : DashboardAction
    data class OnPendingActivityClick(val groupActivityId: String) : DashboardAction
    data class OnPendingActivityDeleteClick(val groupActivityId: String) : DashboardAction
}
