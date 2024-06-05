package com.rafaelboban.activitytracker.ui.screens.activityOverview

import com.rafaelboban.activitytracker.model.network.Activity
import com.rafaelboban.activitytracker.model.network.GroupActivityOverview

data class ActivityOverviewState(
    val activity: Activity? = null,
    val groupActivityOverview: GroupActivityOverview? = null,
    val isLoading: Boolean = true
)
