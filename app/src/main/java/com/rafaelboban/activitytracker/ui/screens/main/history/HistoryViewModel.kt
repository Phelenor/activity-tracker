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
            state = state.copy(isRefreshing = true)

            val activities = activityRepository.getActivities().getOrNull()?.toImmutableList()

            state = state.copy(
                isRefreshing = false,
                activities = activities ?: state.activities
            )
        }
    }

    private fun deleteActivity(id: String) {
        viewModelScope.launch {
            state = state.copy(
                activities = (state.activities - state.activities.first { it.id == id }).toImmutableList()
            )

            activityRepository.deleteActivity(id)
        }
    }
}
