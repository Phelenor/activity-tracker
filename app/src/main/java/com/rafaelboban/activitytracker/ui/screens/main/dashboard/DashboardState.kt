package com.rafaelboban.activitytracker.ui.screens.main.dashboard

import com.rafaelboban.activitytracker.model.network.GroupActivity
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class DashboardState(
    val showSelectActivityBottomSheet: Boolean = false,
    val showConfigureGroupActivityBottomSheet: Boolean = false,
    val showJoinGroupActivityBottomSheet: Boolean = false,
    val shouldShowLocationPermissionRationale: Boolean = false,
    val shouldShowCameraPermissionRationale: Boolean = false,
    val isCreatingGroupActivity: Boolean = false,
    val isJoiningGroupActivity: Boolean = false,
    val pendingActivities: ImmutableList<GroupActivity> = persistentListOf()
)
