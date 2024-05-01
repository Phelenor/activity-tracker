package com.rafaelboban.activitytracker.ui.screens.main.profile

sealed interface ProfileEvent {
    data object DeleteAccountSuccess : ProfileEvent
    data object DeleteAccountError : ProfileEvent
    data object NameChangeSuccess : ProfileEvent
    data object NameChangeError : ProfileEvent
    data object LogoutSuccess : ProfileEvent
}
