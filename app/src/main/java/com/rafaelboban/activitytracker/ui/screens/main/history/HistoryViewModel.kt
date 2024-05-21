package com.rafaelboban.activitytracker.ui.screens.main.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.network.repository.ActivityRepository
import com.skydoves.sandwich.getOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    var state by mutableStateOf(HistoryState())
        private set

    init {
        getActivities()
    }

    fun onAction(action: HistoryAction) {
        when (action) {
            HistoryAction.Refresh -> getActivities()
            is HistoryAction.DeleteActivity -> deleteActivity(action.id)
        }
    }

    private fun getActivities() {
        viewModelScope.launch {
            val loaderDelay = async { delay(500) }

            state = state.copy(isRefreshing = true)

            val activities = activityRepository.getActivities().getOrNull()?.toImmutableList()
            val displayedActivities = activities ?: state.activities

            state = state.copy(
                activities = displayedActivities,
                showEmptyState = displayedActivities.isEmpty()
            )

            loaderDelay.await()
            state = state.copy(
                isRefreshing = false
            )
        }
    }

    private fun deleteActivity(id: String) {
        viewModelScope.launch {
            val updatedActivities = state.activities - state.activities.first { it.id == id }

            state = state.copy(
                activities = updatedActivities.toImmutableList(),
                showEmptyState = updatedActivities.isEmpty()
            )

            activityRepository.deleteActivity(id)
        }
    }
}
