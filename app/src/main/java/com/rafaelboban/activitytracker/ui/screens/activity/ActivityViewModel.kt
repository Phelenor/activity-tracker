package com.rafaelboban.activitytracker.ui.screens.activity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.service.ActivityTrackerService
import com.rafaelboban.core.tracker.model.ActivityStatus
import com.rafaelboban.core.tracker.model.ActivityStatus.Companion.isRunning
import com.rafaelboban.core.tracker.tracking.ActivityTracker
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

    var state by mutableStateOf(
        ActivityState(
            isActive = ActivityTrackerService.isActive && tracker.isActive.value,
            activityStatus = if (ActivityTrackerService.isActive) tracker.activityStatus.value else ActivityStatus.NOT_STARTED
        )
    )
        private set

    private val eventChannel = Channel<ActivityEvent>()
    val events = eventChannel.receiveAsFlow()

    private val isActive = snapshotFlow {
        state.isActive
    }.stateIn(viewModelScope, SharingStarted.Lazily, state.isActive)

    private val activityStatus = snapshotFlow {
        state.activityStatus
    }.stateIn(viewModelScope, SharingStarted.Lazily, state.activityStatus)

    init {
        tracker.startTrackingLocation()

        isActive.onEach { isActive ->
            tracker.setIsActive(isActive)
        }.launchIn(viewModelScope)

        activityStatus.onEach { status ->
            tracker.setStatus(status)
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
                viewModelScope.launch {
                    eventChannel.trySend(ActivityEvent.NavigateBack)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        if (ActivityTrackerService.isActive.not()) {
            tracker.clear()
        }
    }
}
