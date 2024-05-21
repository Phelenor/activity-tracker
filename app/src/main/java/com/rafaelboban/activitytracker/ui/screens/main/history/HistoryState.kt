package com.rafaelboban.activitytracker.ui.screens.main.history

import com.rafaelboban.activitytracker.model.network.Activity
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class HistoryState(
    val activities: ImmutableList<Activity> = persistentListOf(),
    val isRefreshing: Boolean = false
)
