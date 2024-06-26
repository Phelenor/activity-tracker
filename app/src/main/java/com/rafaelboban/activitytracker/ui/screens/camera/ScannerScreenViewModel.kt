package com.rafaelboban.activitytracker.ui.screens.camera

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.network.repository.ActivityRepository
import com.rafaelboban.activitytracker.network.repository.GymRepository
import com.skydoves.sandwich.suspendOnFailure
import com.skydoves.sandwich.suspendOnSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannerScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val activityRepository: ActivityRepository,
    private val gymRepository: GymRepository
) : ViewModel() {

    private val scannerType = checkNotNull(savedStateHandle.get<Int>("scannerTypeOrdinal")?.let { ScannerType.entries[it] })

    var state by mutableStateOf(ScannerScreenState())
        private set

    private val eventChannel = Channel<ScannerScreenEvent>()
    val events = eventChannel.receiveAsFlow()

    private val urlRegex by lazy {
        when (scannerType) {
            ScannerType.GROUP_ACTIVITY -> Regex("^activity_tracker://group_activity/([0-9A-F]{6})$")
            ScannerType.GYM_ACTIVITY,
            ScannerType.GYM_EQUIPMENT -> Regex("^activity_tracker://gym_equipment/(.{36})$")
        }
    }

    private var lastCode = ""

    fun processBarcodeText(text: String) {
        if (state.isScanningEnabled.not() || state.isCheckingDataValidity) return

        state = state.copy(isScanningEnabled = false)

        if (urlRegex.matches(text)) {
            viewModelScope.launch {
                val code = urlRegex.find(text)?.groups?.get(1)?.value

                if (code != null) {
                    if (code == lastCode) return@launch
                    lastCode = code

                    when (scannerType) {
                        ScannerType.GROUP_ACTIVITY -> handleGroupActivity(code)
                        ScannerType.GYM_ACTIVITY -> handleGymEquipment(code) { id -> ScannerScreenEvent.GymActivityJoinSuccess(id) }
                        ScannerType.GYM_EQUIPMENT -> handleGymEquipment(code) { id -> ScannerScreenEvent.EquipmentScanSuccess(id) }
                    }
                } else {
                    state = state.copy(isScanningEnabled = true)
                }
            }
        } else {
            state = state.copy(isScanningEnabled = false)
        }
    }

    private suspend fun handleGroupActivity(joinCode: String) = coroutineScope {
        state = state.copy(isCheckingDataValidity = true)

        activityRepository.joinGroupActivity(joinCode).suspendOnSuccess {
            eventChannel.trySend(ScannerScreenEvent.GroupActivityJoinSuccess(data.id))
        }.suspendOnFailure {
            eventChannel.trySend(ScannerScreenEvent.GroupActivityJoinFailure)
            state = state.copy(isCheckingDataValidity = false, isScanningEnabled = true)
        }
    }

    private suspend fun handleGymEquipment(id: String, onSuccess: (id: String) -> ScannerScreenEvent) = coroutineScope {
        state = state.copy(isCheckingDataValidity = true)

        gymRepository.checkIfEquipmentExists(id).suspendOnSuccess {
            eventChannel.trySend(onSuccess(id))
        }.suspendOnFailure {
            eventChannel.trySend(ScannerScreenEvent.EquipmentScanFailure)
            state = state.copy(isCheckingDataValidity = false, isScanningEnabled = true)
        }
    }
}

enum class ScannerType(val urlFormat: String) {
    GROUP_ACTIVITY("activity_tracker://group_activity/"),
    GYM_ACTIVITY("activity_tracker://gym_activity/"),
    GYM_EQUIPMENT("activity_tracker://gym_equipment/")
}
