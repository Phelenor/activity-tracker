package com.rafaelboban.activitytracker.ui.screens.gymActivity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.model.network.FetchStatus
import com.rafaelboban.activitytracker.network.repository.GymRepository
import com.rafaelboban.core.shared.model.ActivityStatus.Companion.isActive
import com.skydoves.sandwich.onFailure
import com.skydoves.sandwich.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GymActivityViewModel @Inject constructor(
    private val gymRepository: GymRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val id: String = checkNotNull(savedStateHandle["id"])

    var state by mutableStateOf(GymActivityState())
        private set

    private val eventChannel = Channel<GymActivityEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        getGymEquipment()
    }

    private fun getGymEquipment() {
        viewModelScope.launch {
            state = state.copy(gymEquipmentFetchStatus = FetchStatus.IN_PROGRESS)

            gymRepository.getEquipment(id).onSuccess {
                state = state.copy(gymEquipment = data, gymEquipmentFetchStatus = FetchStatus.SUCCESS)
                // TODO: socket connect
            }.onFailure {
                state = state.copy(gymEquipmentFetchStatus = FetchStatus.ERROR)
            }
        }
    }

    fun onAction(action: GymActivityAction) {
        when (action) {
            GymActivityAction.DiscardActivity -> {
                viewModelScope.launch {
                    eventChannel.trySend(GymActivityEvent.NavigateBack)
                }
            }

            GymActivityAction.DismissDialogs -> {
                state = state.copy(
                    showDiscardDialog = false,
                    showDoYouWantToSaveDialog = false
                )
            }

            GymActivityAction.OnBackClick -> {
                state = state.copy(showDiscardDialog = state.status.isActive)
            }

            GymActivityAction.RetryGymActivityFetch -> {
                getGymEquipment()
            }

            GymActivityAction.OnFinishClick -> TODO()
            GymActivityAction.OnPauseClick -> TODO()
            GymActivityAction.OnResumeClick -> TODO()
            GymActivityAction.OnStartClick -> TODO()

            GymActivityAction.SaveActivity -> TODO()
        }
    }
}
