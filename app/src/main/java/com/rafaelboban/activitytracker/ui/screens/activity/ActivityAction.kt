package com.rafaelboban.activitytracker.ui.screens.activity

import com.google.maps.android.compose.MapType

sealed interface ActivityAction {
    data object OnBackClick : ActivityAction
    data object OnStartClick : ActivityAction
    data object OnResumeClick : ActivityAction
    data object OnPauseClick : ActivityAction
    data object OnFinishClick : ActivityAction
    data object DismissDialogs : ActivityAction
    data object DiscardActivity : ActivityAction
    data object OnCameraLockToggle : ActivityAction
    data object OnOpenSelectMapType : ActivityAction
    data object OnReloadWeather : ActivityAction
    data class OnSelectMapType(val type: MapType) : ActivityAction
}
