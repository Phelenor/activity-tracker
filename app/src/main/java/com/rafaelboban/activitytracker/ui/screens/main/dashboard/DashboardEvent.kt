package com.rafaelboban.activitytracker.ui.screens.main.dashboard

import com.rafaelboban.core.shared.model.ActivityType

sealed interface DashboardEvent {

    data class GroupActivityCreated(val groupActivityId: String, val activityType: ActivityType) : DashboardEvent

    data class JoinActivitySuccess(val groupActivityId: String, val activityType: ActivityType) : DashboardEvent

    data object GroupActivityCreationError : DashboardEvent

    data object GroupActivityJoinError : DashboardEvent
}
