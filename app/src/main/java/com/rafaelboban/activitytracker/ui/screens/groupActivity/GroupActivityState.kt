package com.rafaelboban.activitytracker.ui.screens.groupActivity

import com.google.maps.android.compose.MapType
import com.rafaelboban.activitytracker.model.ActivityData
import com.rafaelboban.activitytracker.model.location.Location
import com.rafaelboban.activitytracker.model.network.FetchStatus
import com.rafaelboban.activitytracker.model.network.GroupActivity
import com.rafaelboban.activitytracker.network.model.weather.WeatherForecast
import com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.components.ActivityTabType
import com.rafaelboban.core.shared.model.ActivityStatus
import kotlin.time.Duration

data class GroupActivityState(
    val activityData: ActivityData = ActivityData(),
    val selectedBottomSheetTab: ActivityTabType = ActivityTabType.DETAILS,
    val duration: Duration = Duration.ZERO,
    val currentLocation: Location? = null,
    val showDiscardDialog: Boolean = false,
    val showDoYouWantToSaveDialog: Boolean = false,
    val showShareDialog: Boolean = false,
    val status: ActivityStatus = ActivityStatus.NOT_STARTED,
    val mapCameraLocked: Boolean = true,
    val showSelectMapTypeDialog: Boolean = false,
    val mapType: MapType = MapType.NORMAL,
    val maxSpeed: Float = Float.MIN_VALUE,
    val weather: WeatherForecast? = null,
    val isWeatherLoading: Boolean = false,
    val isSaving: Boolean = false,
    val groupActivity: GroupActivity? = null,
    val groupActivityFetchStatus: FetchStatus = FetchStatus.IN_PROGRESS,
    val isActivityOwner: Boolean = false
)
