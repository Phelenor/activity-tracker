package com.rafaelboban.activitytracker.ui.screens.main.profile

sealed interface ProfileAction {
    data object OnLogoutClick : ProfileAction
    data object OnDeleteAccountClick : ProfileAction
    data object OnChangeNameClick : ProfileAction
    data object ConfirmLogout : ProfileAction
    data object ConfirmDeleteAccount : ProfileAction
    data object DismissDialog : ProfileAction
    data object OnHeightClick : ProfileAction
    data object OnWeightClick : ProfileAction
    data object OnAgeClick : ProfileAction
    data class ConfirmChangeName(val name: String) : ProfileAction
    data class ConfirmHeightClick(val height: Int) : ProfileAction
    data class ConfirmWeightClick(val weight: Int) : ProfileAction
    data class ConfirmBirthDateClick(val timestamp: Long) : ProfileAction
}
