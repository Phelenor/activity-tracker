package com.rafaelboban.activitytracker.ui.screens.main.dashboard

data class DashboardState(
    val showSelectActivityBottomSheet: Boolean = false,
    val showConfigureGroupActivityBottomSheet: Boolean = false,
    val shouldShowPermissionRationale: Boolean = false,
    val isCreatingGroupActivity: Boolean = false
)
