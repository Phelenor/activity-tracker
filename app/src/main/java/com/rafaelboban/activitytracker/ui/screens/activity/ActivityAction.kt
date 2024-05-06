package com.rafaelboban.activitytracker.ui.screens.activity

sealed interface ActivityAction {
    data object OnBackClick : ActivityAction
    data object OnStartClick : ActivityAction
    data object OnResumeClick : ActivityAction
    data object OnPauseClick : ActivityAction
    data object OnFinishClick : ActivityAction
    data object DismissDiscardDialog : ActivityAction
    data object DiscardActivity : ActivityAction
}
