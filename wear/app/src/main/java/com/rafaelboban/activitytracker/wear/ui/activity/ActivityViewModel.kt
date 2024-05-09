package com.rafaelboban.activitytracker.wear.ui.activity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor() : ViewModel() {

    var state by mutableStateOf(ActivityState())
        private set

    fun onAction(action: ActivityAction) {
    }
}
