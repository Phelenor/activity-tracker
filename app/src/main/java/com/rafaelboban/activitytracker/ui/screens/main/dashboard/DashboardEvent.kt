package com.rafaelboban.activitytracker.ui.screens.main.dashboard

sealed interface DashboardEvent {

    data class GroupActivityCreated(val groupActivityId: String) : DashboardEvent

    data class JoinActivitySuccess(val groupActivityId: String) : DashboardEvent

    data object GroupActivityCreationError : DashboardEvent

    data object GroupActivityJoinError : DashboardEvent
}
