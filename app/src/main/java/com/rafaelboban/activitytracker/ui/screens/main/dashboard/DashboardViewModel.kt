package com.rafaelboban.activitytracker.ui.screens.main.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.network.repository.ActivityRepository
import com.rafaelboban.core.shared.model.ActivityType
import com.skydoves.sandwich.message
import com.skydoves.sandwich.onFailure
import com.skydoves.sandwich.suspendOnSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
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
            showConfigureGroupActivityBottomSheet = false
        )
    }

    fun showSelectActivityBottomSheet() {
        state = state.copy(showSelectActivityBottomSheet = true)
    }

    fun showConfigureGroupActivityBottomSheet() {
        state = state.copy(showConfigureGroupActivityBottomSheet = true)
    }

    fun displayLocationRationaleDialog(isVisible: Boolean) {
        state = state.copy(shouldShowLocationPermissionRationale = isVisible)
    }

    fun shouldShowCameraPermissionRationale(isVisible: Boolean) {
        state = state.copy(shouldShowCameraPermissionRationale = isVisible)
    }

    fun createGroupActivity(type: ActivityType, estimatedStartTimestamp: Long?) {
        viewModelScope.launch {
            state = state.copy(isCreatingGroupActivity = true)

            activityRepository.createGroupActivity(type, estimatedStartTimestamp).suspendOnSuccess {
                eventChannel.trySend(DashboardEvent.ActivityCreated(data.id, data.activityType))
            }.onFailure {
                Timber.e("Activity creation error: ${message()}")
            }

            state = state.copy(isCreatingGroupActivity = false)
        }
    }
}
