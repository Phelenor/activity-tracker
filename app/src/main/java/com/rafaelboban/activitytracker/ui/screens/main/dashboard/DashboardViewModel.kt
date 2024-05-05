package com.rafaelboban.activitytracker.ui.screens.main.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {

    var state by mutableStateOf(DashboardState())

    fun dismissBottomSheet() {
        state = state.copy(showSelectActivityBottomSheet = false)
    }

    fun showSelectActivityBottomSheet() {
        state = state.copy(showSelectActivityBottomSheet = true)
    }

    fun displayRationaleDialog(isVisible: Boolean) {
        state = state.copy(shouldShowPermissionRationale = isVisible)
    }
}
