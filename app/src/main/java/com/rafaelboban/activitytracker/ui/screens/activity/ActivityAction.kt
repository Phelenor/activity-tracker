package com.rafaelboban.activitytracker.ui.screens.activity

import com.google.maps.android.compose.MapType
import com.rafaelboban.activitytracker.network.model.goals.ActivityGoal
import com.rafaelboban.activitytracker.network.model.goals.ActivityGoalType
import com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.components.ActivityTabType

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
    data object OpenGoals : ActivityAction
    data object OnAddGoalClick : ActivityAction
    data object SaveActivity : ActivityAction
    data class AddGoal(val goal: ActivityGoal) : ActivityAction
    data class RemoveGoal(val goal: ActivityGoalType) : ActivityAction
    data class OnTabChanged(val tab: ActivityTabType) : ActivityAction
    data class DismissGoalsDialog(val doNotShowAgain: Boolean) : ActivityAction
    data class OnSelectMapType(val type: MapType) : ActivityAction
    class MapSnapshotDone(val stream: ByteArray) : ActivityAction
}
