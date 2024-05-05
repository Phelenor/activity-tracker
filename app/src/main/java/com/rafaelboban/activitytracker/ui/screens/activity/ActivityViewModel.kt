package com.rafaelboban.activitytracker.ui.screens.activity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.tracking.ActivityTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val tracker: ActivityTracker
) : ViewModel() {

    var state by mutableStateOf(ActivityState())
        private set

    private val isActive = snapshotFlow { state.isActive }.stateIn(viewModelScope, SharingStarted.Lazily, state.isActive)

    init {
        tracker.startTrackingLocation()

        isActive.onEach { isActive ->
            tracker.setIsActive(isActive)
        }.launchIn(viewModelScope)

        tracker.currentLocation.onEach { currentLocation ->
            state = state.copy(currentLocation = currentLocation?.location)
        }.launchIn(viewModelScope)

        tracker.activityData.onEach { data ->
            state = state.copy(activityData = data)
        }.launchIn(viewModelScope)

        tracker.duration.onEach { duration ->
            state = state.copy(duration = duration)
        }.launchIn(viewModelScope)
    }
}
