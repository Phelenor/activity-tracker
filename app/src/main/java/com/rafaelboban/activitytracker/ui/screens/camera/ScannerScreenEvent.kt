package com.rafaelboban.activitytracker.ui.screens.camera

sealed interface ScannerScreenEvent {

    data class GroupActivityJoinSuccess(val activityId: String) : ScannerScreenEvent
    data object GroupActivityJoinFailure : ScannerScreenEvent
}
