package com.rafaelboban.activitytracker.ui.screens.main.dashboard

data class DashboardState(
    val showSelectActivityBottomSheet: Boolean = false,
    val showConfigureGroupActivityBottomSheet: Boolean = false,
    val showJoinGroupActivityBottomSheet: Boolean = false,
    val shouldShowLocationPermissionRationale: Boolean = false,
    val shouldShowCameraPermissionRationale: Boolean = false,
    val isCreatingGroupActivity: Boolean = false,
    val isJoiningGroupActivity: Boolean = false
)
