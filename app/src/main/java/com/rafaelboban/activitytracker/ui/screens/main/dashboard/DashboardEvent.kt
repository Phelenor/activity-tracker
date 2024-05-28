package com.rafaelboban.activitytracker.ui.screens.main.dashboard

import com.rafaelboban.core.shared.model.ActivityType

sealed interface DashboardEvent {

    data class ActivityCreated(val groupActivityId: String, val activityType: ActivityType) : DashboardEvent
}
