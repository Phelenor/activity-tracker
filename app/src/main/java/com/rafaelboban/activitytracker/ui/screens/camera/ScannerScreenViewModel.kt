package com.rafaelboban.activitytracker.ui.screens.camera

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannerScreenViewModel @Inject constructor() : ViewModel() {

    var scanningEnabled by mutableStateOf(true)
        private set

    private val eventChannel = Channel<ScannerScreenEvent>()
    val events = eventChannel.receiveAsFlow()

    private val joinCodeUrlRegex by lazy { Regex("^activity_tracker://group_activity/(\\d{8})$") }

    fun processBarcodeText(text: String) {
        scanningEnabled = false

        if (joinCodeUrlRegex.matches(text)) {
            viewModelScope.launch {
                val joinCode = joinCodeUrlRegex.find(text)?.groups?.get(1)?.value
                if (joinCode != null) {
                    eventChannel.trySend(ScannerScreenEvent.JoinCodeFound(joinCode))
                } else {
                    scanningEnabled = true
                }
            }
        } else {
            scanningEnabled = true
        }
    }
}
