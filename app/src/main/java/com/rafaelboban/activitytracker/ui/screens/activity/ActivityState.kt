package com.rafaelboban.activitytracker.ui.screens.activity

import com.google.maps.android.compose.MapType
import com.rafaelboban.activitytracker.model.ActivityData
import com.rafaelboban.activitytracker.model.location.Location
import com.rafaelboban.activitytracker.network.model.goals.ActivityGoalProgress
import com.rafaelboban.activitytracker.network.model.weather.WeatherForecast
import com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.components.ActivityTabType
import com.rafaelboban.core.shared.model.ActivityStatus
import com.rafaelboban.core.shared.model.ActivityType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Duration

data class ActivityState(
    val selectedBottomSheetTab: ActivityTabType = ActivityTabType.DETAILS,
    val activityData: ActivityData = ActivityData(),
    val duration: Duration = Duration.ZERO,
    val currentLocation: Location? = null,
    val showDiscardDialog: Boolean = false,
    val status: ActivityStatus = ActivityStatus.NOT_STARTED,
    val mapCameraLocked: Boolean = true,
    val showSelectMapTypeDialog: Boolean = false,
    val showSetGoalsDialog: Boolean = false,
    val mapType: MapType = MapType.NORMAL,
    val maxSpeed: Float = Float.MIN_VALUE,
    val weather: WeatherForecast? = null,
    val isWeatherLoading: Boolean = false,
    val goals: ImmutableList<ActivityGoalProgress> = persistentListOf(),
    val type: ActivityType
)
