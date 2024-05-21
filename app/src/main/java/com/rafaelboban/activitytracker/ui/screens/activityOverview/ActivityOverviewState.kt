package com.rafaelboban.activitytracker.ui.screens.activityOverview

import com.rafaelboban.activitytracker.model.network.Activity

data class ActivityOverviewState(
    val activity: Activity? = null,
    val isLoading: Boolean = true
)
