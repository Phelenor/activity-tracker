package com.rafaelboban.activitytracker.ui.screens.gymEquipment

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.network.repository.GymRepository
import com.skydoves.sandwich.onFailure
import com.skydoves.sandwich.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GymEquipmentViewModel @Inject constructor(
    private val gymRepository: GymRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val id: String = checkNotNull(savedStateHandle["id"])

    var state by mutableStateOf(GymEquipmentScreenState())
        private set

    init {
        getEquipment()
    }

    fun getEquipment() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            gymRepository.getEquipment(id).onSuccess {
                state = state.copy(isLoading = false, equipment = data)
            }.onFailure {
                state = state.copy(isLoading = false)
            }
        }
    }
}
