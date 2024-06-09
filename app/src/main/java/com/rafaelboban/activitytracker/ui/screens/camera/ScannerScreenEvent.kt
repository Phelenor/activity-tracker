package com.rafaelboban.activitytracker.ui.screens.camera

sealed interface ScannerScreenEvent {

    data class GroupActivityJoinSuccess(val activityId: String) : ScannerScreenEvent
    data object GroupActivityJoinFailure : ScannerScreenEvent

    data class EquipmentScanSuccess(val equipmentId: String) : ScannerScreenEvent
    data object EquipmentScanFailure : ScannerScreenEvent

    data class GymActivityJoinSuccess(val activityId: String) : ScannerScreenEvent
    data object GymActivityJoinFailure : ScannerScreenEvent
}
