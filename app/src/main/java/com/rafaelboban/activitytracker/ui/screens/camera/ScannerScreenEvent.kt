package com.rafaelboban.activitytracker.ui.screens.camera

sealed interface ScannerScreenEvent {

    data class JoinCodeFound(val joinCode: String) : ScannerScreenEvent
}
