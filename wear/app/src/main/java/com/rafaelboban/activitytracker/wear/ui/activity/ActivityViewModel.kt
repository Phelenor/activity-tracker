package com.rafaelboban.activitytracker.wear.ui.activity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.wear.tracker.HealthServicesExerciseTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val exerciseTracker: HealthServicesExerciseTracker
) : ViewModel() {

    var state by mutableStateOf(ActivityState())
        private set

    private val canTrackHeartRate = snapshotFlow {
        state.canTrackHeartRate
    }.stateIn(viewModelScope, SharingStarted.Lazily, state.canTrackHeartRate)

    init {
        canTrackHeartRate.flatMapLatest { canTrack ->
            if (canTrack) exerciseTracker.heartRate else flowOf()
        }.onEach { heartRate ->
            state = state.copy(heartRate = heartRate)
        }.launchIn(viewModelScope)
    }

    fun onAction(action: ActivityAction) {
        when (action) {
            ActivityAction.GrantBodySensorsPermission -> {
                viewModelScope.launch {
                    state = state.copy(canTrackHeartRate = exerciseTracker.isHeartRateTrackingSupported())
                }
            }
        }
    }

    override fun onCleared() {
        viewModelScope.launch {
            exerciseTracker.stopExercise()
        }

        super.onCleared()
    }
}
