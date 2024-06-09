package com.rafaelboban.activitytracker.ui.screens.gymActivity


sealed interface GymActivityAction {
    data object OnBackClick : GymActivityAction
    data object OnStartClick : GymActivityAction
    data object OnResumeClick : GymActivityAction
    data object OnPauseClick : GymActivityAction
    data object OnFinishClick : GymActivityAction
    data object DismissDialogs : GymActivityAction
    data object DiscardActivity : GymActivityAction
    data object SaveActivity : GymActivityAction
    data object RetryGymActivityFetch : GymActivityAction
}
