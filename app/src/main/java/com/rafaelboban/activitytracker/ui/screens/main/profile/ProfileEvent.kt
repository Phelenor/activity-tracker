package com.rafaelboban.activitytracker.ui.screens.main.profile

sealed interface ProfileEvent {
    data object DeleteAccountSuccess : ProfileEvent
    data object DeleteAccountError : ProfileEvent
    data object UserInfoChangeSuccess : ProfileEvent
    data object UserInfoChangeError : ProfileEvent
    data object LogoutSuccess : ProfileEvent
}
