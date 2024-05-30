package com.rafaelboban.activitytracker.ui.screens.main.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.network.repository.ActivityRepository
import com.rafaelboban.core.shared.model.ActivityType
import com.skydoves.sandwich.onFailure
import com.skydoves.sandwich.suspendOnSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    var state by mutableStateOf(DashboardState())

    private val eventChannel = Channel<DashboardEvent>()
    val events = eventChannel.receiveAsFlow()

    fun dismissBottomSheet() {
        state = state.copy(
            showSelectActivityBottomSheet = false,
            showConfigureGroupActivityBottomSheet = false,
            showJoinGroupActivityBottomSheet = false
        )
    }

    fun showSelectActivityBottomSheet() {
        state = state.copy(showSelectActivityBottomSheet = true)
    }

    fun showConfigureGroupActivityBottomSheet() {
        state = state.copy(showConfigureGroupActivityBottomSheet = true)
    }

    fun showJoinGroupActivityBottomSheet() {
        state = state.copy(showJoinGroupActivityBottomSheet = true)
    }

    fun displayLocationRationaleDialog(isVisible: Boolean) {
        state = state.copy(shouldShowLocationPermissionRationale = isVisible)
    }

    fun displayCameraPermissionRationale(isVisible: Boolean) {
        state = state.copy(shouldShowCameraPermissionRationale = isVisible)
    }

    fun createGroupActivity(type: ActivityType, estimatedStartTimestamp: Long?) {
        viewModelScope.launch {
            state = state.copy(isCreatingGroupActivity = true)

            activityRepository.createGroupActivity(type, estimatedStartTimestamp).suspendOnSuccess {
                eventChannel.trySend(DashboardEvent.GroupActivityCreated(data.id))
            }.onFailure {
                eventChannel.trySend(DashboardEvent.GroupActivityCreationError)
            }

            state = state.copy(isCreatingGroupActivity = false)
        }
    }

    fun joinGroupActivity(joinCode: String) {
        viewModelScope.launch {
            state = state.copy(isJoiningGroupActivity = true)

            activityRepository.joinGroupActivity(joinCode).suspendOnSuccess {
                eventChannel.trySend(DashboardEvent.GroupActivityCreated(data.id))
            }.onFailure {
                eventChannel.trySend(DashboardEvent.GroupActivityJoinError)
            }

            state = state.copy(isJoiningGroupActivity = false)
        }
    }
}
