package com.rafaelboban.activitytracker.ui.screens.camera

sealed interface ScannerScreenAction {

    data object OnBackPress : ScannerScreenAction

    data class OnScanSuccessful(val text: String) : ScannerScreenAction
}
