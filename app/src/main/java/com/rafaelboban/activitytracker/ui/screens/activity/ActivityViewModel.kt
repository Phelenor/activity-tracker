package com.rafaelboban.activitytracker.ui.screens.activity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.tracking.ActivityTracker
import com.rafaelboban.activitytracker.ui.screens.activity.ActivityStatus.Companion.isRunning
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val tracker: ActivityTracker
) : ViewModel() {

    var state by mutableStateOf(ActivityState())
        private set

    private val eventChannel = Channel<ActivityEvent>()
    val events = eventChannel.receiveAsFlow()

    private val isActive = snapshotFlow {
        state.isActive
    }.stateIn(viewModelScope, SharingStarted.Lazily, state.isActive)

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

    fun onAction(action: ActivityAction) {
        when (action) {
            ActivityAction.OnStartClick -> state = state.copy(
                activityStatus = ActivityStatus.IN_PROGRESS,
                isActive = true
            )

            ActivityAction.OnPauseClick -> state = state.copy(
                activityStatus = ActivityStatus.PAUSED,
                isActive = false
            )

            ActivityAction.OnResumeClick -> state = state.copy(
                activityStatus = ActivityStatus.IN_PROGRESS,
                isActive = true
            )

            ActivityAction.OnFinishClick -> {
                state = state.copy(
                    activityStatus = ActivityStatus.FINISHED,
                    isActive = false
                )

                tracker.stop()
            }

            ActivityAction.OnBackClick -> state = state.copy(
                showDiscardDialog = state.activityStatus.isRunning
            )

            ActivityAction.DismissDiscardDialog -> state = state.copy(
                showDiscardDialog = false
            )

            ActivityAction.DiscardActivity -> {
                state = state.copy(
                    showDiscardDialog = false
                )

                tracker.clear()

                viewModelScope.launch {
                    eventChannel.trySend(ActivityEvent.NavigateBack)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        tracker.clear()
    }
}
