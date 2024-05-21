package com.rafaelboban.activitytracker.ui.screens.activityOverview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.network.repository.ActivityRepository
import com.skydoves.sandwich.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityOverviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val id: String = checkNotNull(savedStateHandle["id"])

    var state by mutableStateOf(ActivityOverviewState())
        private set

    init {
        getActivity()
    }

    private fun getActivity() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            activityRepository.getActivity(id).onSuccess {
                state = state.copy(activity = data)
            }

            state = state.copy(isLoading = false)
        }
    }
}
