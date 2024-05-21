package com.rafaelboban.activitytracker.ui.screens.activityOverview

sealed interface ActivityOverviewAction {

    data object OnBackClick : ActivityOverviewAction
}
