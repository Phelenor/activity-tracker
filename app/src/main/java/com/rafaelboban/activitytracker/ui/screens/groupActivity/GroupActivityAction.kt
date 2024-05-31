package com.rafaelboban.activitytracker.ui.screens.groupActivity

import com.google.maps.android.compose.MapType
import com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.components.ActivityTabType

sealed interface GroupActivityAction {
    data object OnBackClick : GroupActivityAction
    data object OnStartClick : GroupActivityAction
    data object OnResumeClick : GroupActivityAction
    data object OnPauseClick : GroupActivityAction
    data object OnFinishClick : GroupActivityAction
    data object OnConfirmFinishClick : GroupActivityAction
    data object DismissDialogs : GroupActivityAction
    data object DiscardActivity : GroupActivityAction
    data object OnCameraLockToggle : GroupActivityAction
    data object OnOpenSelectMapType : GroupActivityAction
    data object OnShareClick : GroupActivityAction
    data object OnReloadWeather : GroupActivityAction
    data object SaveActivity : GroupActivityAction
    data object RetryGroupActivityFetch : GroupActivityAction
    data class OnTabChanged(val tab: ActivityTabType) : GroupActivityAction
    data class OnSelectMapType(val type: MapType) : GroupActivityAction
    class MapSnapshotDone(val stream: ByteArray) : GroupActivityAction
}
