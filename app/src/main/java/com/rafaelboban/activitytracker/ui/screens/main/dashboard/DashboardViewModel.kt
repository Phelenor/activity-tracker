package com.rafaelboban.activitytracker.ui.screens.main.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.network.repository.ActivityRepository
import com.rafaelboban.core.shared.model.ActivityType
import com.skydoves.sandwich.getOrNull
import com.skydoves.sandwich.onFailure
import com.skydoves.sandwich.suspendOnSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    var state by mutableStateOf(DashboardState())

    private val eventChannel = Channel<DashboardEvent>()
    val events = eventChannel.receiveAsFlow()

    private var refreshJob: Job? = null

    init {
        refreshJob = startRefreshJob(triggerRefreshOnStart = true)
    }

    private fun startRefreshJob(triggerRefreshOnStart: Boolean = false) = viewModelScope.launch {
        if (triggerRefreshOnStart) {
            getScheduledActivities()
        }

        while (isActive) {
            delay(5.minutes)
            getScheduledActivities()
        }
    }

    fun getScheduledActivities() {
        viewModelScope.launch {
            val activities = activityRepository.getGroupActivities().getOrNull()?.toImmutableList()
            val displayedActivities = activities ?: state.scheduledActivities

            state = state.copy(
                scheduledActivities = displayedActivities
            )
        }
    }

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = startRefreshJob()

        viewModelScope.launch {
            val loaderDelay = async { delay(500) }

            state = state.copy(isRefreshing = true)

            val activities = activityRepository.getGroupActivities().getOrNull()?.toImmutableList()
            val displayedActivities = activities ?: state.scheduledActivities

            state = state.copy(
                scheduledActivities = displayedActivities
            )

            loaderDelay.await()
            state = state.copy(isRefreshing = false)
        }
    }

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

    fun deleteScheduledActivity(groupActivityId: String) {
        viewModelScope.launch {
            val updatedActivities = state.scheduledActivities - state.scheduledActivities.first { it.id == groupActivityId }

            state = state.copy(
                scheduledActivities = updatedActivities.toImmutableList()
            )

            activityRepository.deleteGroupActivity(groupActivityId)
        }
    }

    fun leaveScheduledActivity(groupActivityId: String) {
        viewModelScope.launch {
            val updatedActivities = state.scheduledActivities - state.scheduledActivities.first { it.id == groupActivityId }

            state = state.copy(
                scheduledActivities = updatedActivities.toImmutableList()
            )

            activityRepository.leaveGroupActivity(groupActivityId)
        }
    }
}
