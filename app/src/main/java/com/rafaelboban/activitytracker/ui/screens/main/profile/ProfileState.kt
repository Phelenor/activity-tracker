package com.rafaelboban.activitytracker.ui.screens.main.profile

import com.rafaelboban.activitytracker.model.User

data class ProfileState(
    val user: User,
    val showChangeNameDialog: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val showDeleteAccountDialog: Boolean = false,
    val submitInProgress: Boolean = false,
    val showWeightDialog: Boolean = false,
    val showHeightDialog: Boolean = false
)
