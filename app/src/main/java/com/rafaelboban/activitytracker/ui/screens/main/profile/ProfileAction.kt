package com.rafaelboban.activitytracker.ui.screens.main.profile

sealed interface ProfileAction {
    data object OnLogoutClick : ProfileAction
    data object OnDeleteAccountClick : ProfileAction
    data object OnChangeNameClick : ProfileAction
    data class ConfirmChangeName(val name: String) : ProfileAction
    data object ConfirmLogout : ProfileAction
    data object ConfirmDeleteAccount : ProfileAction
    data object DismissDialog : ProfileAction
}
