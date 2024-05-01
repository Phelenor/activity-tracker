package com.rafaelboban.activitytracker.ui.screens.main.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {

    var state by mutableStateOf(DashboardState(isLoading = false))
}
