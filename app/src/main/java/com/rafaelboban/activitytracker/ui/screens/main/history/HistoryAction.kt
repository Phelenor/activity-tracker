package com.rafaelboban.activitytracker.ui.screens.main.history

sealed interface HistoryAction {

    data object Refresh : HistoryAction
    data class DeleteActivity(val id: String) : HistoryAction
}
