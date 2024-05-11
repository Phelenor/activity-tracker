package com.rafaelboban.activitytracker.ui.screens.login

import com.rafaelboban.core.shared.ui.util.UiText

sealed interface LoginEvent {

    data class Error(val error: UiText) : LoginEvent

    data object Success : LoginEvent
}
