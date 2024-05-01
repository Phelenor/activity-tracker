package com.rafaelboban.activitytracker.ui.screens.login

import com.rafaelboban.activitytracker.ui.util.UiText


sealed interface LoginEvent {

    data class Error(val error: UiText) : LoginEvent

    data object Success : LoginEvent
}
