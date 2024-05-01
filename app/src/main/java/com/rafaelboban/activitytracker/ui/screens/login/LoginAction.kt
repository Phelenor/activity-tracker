package com.rafaelboban.activitytracker.ui.screens.login

sealed interface LoginAction {
    data object OnLoginClick : LoginAction
}
