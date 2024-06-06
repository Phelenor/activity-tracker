package com.rafaelboban.activitytracker.ui.screens.main.dashboard

import com.rafaelboban.activitytracker.ui.screens.camera.ScannerType
import com.rafaelboban.core.shared.model.ActivityType

sealed interface DashboardAction {
    data object DismissBottomSheet : DashboardAction
    data object DismissRationaleDialog : DashboardAction
    data object RequestPermissions : DashboardAction
    data object OpenSelectActivityTypeIndividualBottomSheet : DashboardAction
    data object OpenJoinGroupActivityBottomSheet : DashboardAction
    data object OpenConfigureGroupActivityBottomSheet : DashboardAction
    data object Refresh : DashboardAction
    data object GetEquipmentInfoClick : DashboardAction
    data object JoinGymActivityClick : DashboardAction
    data class OpenQRCodeScanner(val type: ScannerType) : DashboardAction
    data class JoinGroupActivity(val joinCode: String) : DashboardAction
    data class StartIndividualActivity(val type: ActivityType) : DashboardAction
    data class CreateGroupActivity(val type: ActivityType, val estimatedStartTimestamp: Long?) : DashboardAction
    data class OnScheduledActivityClick(val groupActivityId: String) : DashboardAction
    data class OnScheduledActivityDeleteClick(val groupActivityId: String, val isActivityOwner: Boolean) : DashboardAction
}
