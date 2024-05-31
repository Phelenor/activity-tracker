package com.rafaelboban.activitytracker.ui.screens.activity

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.di.PreferencesStandard
import com.rafaelboban.activitytracker.model.network.Activity
import com.rafaelboban.activitytracker.model.network.ActivityWeatherInfo
import com.rafaelboban.activitytracker.network.model.goals.PREFERENCE_SHOW_GOALS_REMINDER
import com.rafaelboban.activitytracker.network.repository.ActivityRepository
import com.rafaelboban.activitytracker.network.repository.WeatherRepository
import com.rafaelboban.activitytracker.service.ActivityTrackerService
import com.rafaelboban.activitytracker.tracking.ActivityTracker
import com.rafaelboban.activitytracker.ui.shared.WeatherUpdateViewModel
import com.rafaelboban.activitytracker.util.UserData
import com.rafaelboban.activitytracker.util.edit
import com.rafaelboban.core.shared.connectivity.connectors.PhoneToWatchConnector
import com.rafaelboban.core.shared.connectivity.model.MessagingAction
import com.rafaelboban.core.shared.model.ActivityStatus
import com.rafaelboban.core.shared.model.ActivityStatus.Companion.isActive
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.shared.utils.DEFAULT_HEART_RATE_TRACKER_AGE
import com.rafaelboban.core.shared.utils.HeartRateZoneHelper
import com.skydoves.sandwich.suspendOnFailure
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val applicationScope: CoroutineScope,
    private val tracker: ActivityTracker,
    private val watchConnector: PhoneToWatchConnector,
    private val activityRepository: ActivityRepository,
    @PreferencesStandard private val preferences: SharedPreferences,
    weatherRepository: WeatherRepository,
    savedStateHandle: SavedStateHandle
) : WeatherUpdateViewModel(weatherRepository) {

    private val type = checkNotNull(savedStateHandle.get<Int>("activityTypeOrdinal")).let { ordinal -> ActivityType.entries[ordinal] }

    var state by mutableStateOf(ActivityState(status = tracker.status.value, type = type))
        private set

    private val eventChannel = Channel<ActivityEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        tracker.startTrackingLocation(type)

        tracker.currentLocation.onEach { currentLocation ->
            state = state.copy(currentLocation = currentLocation?.location)
        }.launchIn(viewModelScope)

        tracker.data.onEach { data ->
            state = state.copy(
                activityData = data,
                maxSpeed = max(state.maxSpeed, data.speed)
            )
        }.launchIn(viewModelScope)

        tracker.duration.onEach { duration ->
            state = state.copy(duration = duration)
        }.launchIn(viewModelScope)

        tracker.goals.onEach { goals ->
            state = state.copy(goals = goals.toImmutableList())
        }.launchIn(viewModelScope)

        if (preferences.getBoolean(PREFERENCE_SHOW_GOALS_REMINDER, true)) {
            viewModelScope.launch {
                delay(2.seconds)
                state = state.copy(showSetGoalsDialog = true)
            }
        }

        startWeatherUpdates(
            onLoading = { isLoading -> state = state.copy(isWeatherLoading = isLoading) },
            onUpdate = { weather -> state = state.copy(weather = weather) },
            currentLocationGetter = { state.currentLocation }
        )

        listenToWatchActions()
    }

    fun onAction(action: ActivityAction, fromWatch: Boolean = false) {
        if (!fromWatch) {
            sendActionToWatch(action)
        }

        when (action) {
            ActivityAction.OnStartClick -> {
                state = state.copy(status = ActivityStatus.IN_PROGRESS)
                tracker.setActivityStatus(ActivityStatus.IN_PROGRESS)
                tracker.setIsTrackingActivity(true)
            }

            ActivityAction.OnPauseClick -> {
                state = state.copy(status = ActivityStatus.PAUSED)
                tracker.setActivityStatus(ActivityStatus.PAUSED)
                tracker.setIsTrackingActivity(false)
            }

            ActivityAction.OnResumeClick -> {
                state = state.copy(status = ActivityStatus.IN_PROGRESS)
                tracker.setActivityStatus(ActivityStatus.IN_PROGRESS)
                tracker.setIsTrackingActivity(true)
            }

            ActivityAction.OnFinishClick -> {
                val shorterThan30Seconds = state.duration < 30.seconds
                val hasAtLeast5Locations = state.activityData.locations.flatten().size >= 5

                state = state.copy(
                    status = ActivityStatus.FINISHED,
                    isSaving = !shorterThan30Seconds && hasAtLeast5Locations,
                    showDoYouWantToSaveDialog = shorterThan30Seconds && hasAtLeast5Locations
                )

                tracker.setActivityStatus(ActivityStatus.FINISHED)
                tracker.setIsTrackingActivity(false)
                tracker.stopTrackingLocation()
            }

            ActivityAction.OnBackClick -> {
                state = state.copy(showDiscardDialog = state.status.isActive)
            }

            ActivityAction.DismissDialogs -> {
                state = state.copy(
                    showDiscardDialog = false,
                    showSelectMapTypeDialog = false,
                    showAddGoalDialog = false,
                    showSetGoalsDialog = false,
                    showDoYouWantToSaveDialog = false
                )
            }

            ActivityAction.OnCameraLockToggle -> {
                state = state.copy(mapCameraLocked = !state.mapCameraLocked)
            }

            ActivityAction.OnOpenSelectMapType -> {
                state = state.copy(showSelectMapTypeDialog = true)
            }

            ActivityAction.DiscardActivity -> {
                viewModelScope.launch {
                    eventChannel.trySend(ActivityEvent.NavigateBack)
                }
            }

            ActivityAction.OnReloadWeather -> {
                restartWeatherUpdates()
            }

            is ActivityAction.OnTabChanged -> {
                state = state.copy(selectedBottomSheetTab = action.tab)
            }

            ActivityAction.OpenGoals -> {
                viewModelScope.launch {
                    eventChannel.trySend(ActivityEvent.OpenGoals)
                }
            }

            ActivityAction.OnAddGoalClick -> {
                state = state.copy(showAddGoalDialog = true)
            }

            ActivityAction.SaveActivity -> {
                state = state.copy(isSaving = true)
            }

            is ActivityAction.AddGoal -> {
                tracker.addGoal(action.goal)
            }

            is ActivityAction.RemoveGoal -> {
                tracker.removeGoal(action.goal)
            }

            is ActivityAction.DismissGoalsDialog -> {
                state = state.copy(showSetGoalsDialog = false)

                if (action.doNotShowAgain) {
                    preferences.edit { putBoolean(PREFERENCE_SHOW_GOALS_REMINDER, false) }
                }
            }

            is ActivityAction.MapSnapshotDone -> {
                saveActivity(action.stream)
            }

            is ActivityAction.OnSelectMapType -> {
                state = state.copy(mapType = action.type)
            }
        }
    }

    private fun saveActivity(mapSnapshot: ByteArray) {
        viewModelScope.launch {
            val zoneDistribution = HeartRateZoneHelper.calculateHeartRateZoneDistribution(state.activityData.heartRatePoints, UserData.user?.age ?: DEFAULT_HEART_RATE_TRACKER_AGE, state.duration)
            val activity = Activity(
                activityType = state.type,
                startTimestamp = tracker.startTimestamp ?: 0,
                endTimestamp = tracker.endTimestamp ?: 0,
                durationSeconds = state.duration.inWholeSeconds,
                distanceMeters = state.activityData.distanceMeters,
                elevation = state.activityData.elevationGain,
                calories = state.activityData.caloriesBurned ?: 0,
                avgHeartRate = state.activityData.heartRatePoints.map { it.heartRate }.takeIf { it.isNotEmpty() }?.average()?.toInt() ?: 0,
                avgSpeedKmh = state.duration.inWholeSeconds.takeIf { it > 0 }?.let { (state.activityData.distanceMeters / 1000f) / (state.duration.inWholeSeconds / 3600f) } ?: 0f,
                maxHeartRate = state.activityData.heartRatePoints.maxOfOrNull { it.heartRate } ?: 0,
                maxSpeedKmh = state.maxSpeed,
                heartRateZoneDistribution = zoneDistribution ?: emptyMap(),
                goals = state.goals,
                weather = state.weather?.current?.let { weather ->
                    ActivityWeatherInfo(
                        temp = weather.temp,
                        humidity = weather.humidity,
                        icon = weather.info.firstOrNull()?.icon ?: "",
                        description = weather.info.firstOrNull()?.description ?: ""
                    )
                }
            )

            activityRepository.saveActivity(activity, mapSnapshot).suspendOnFailure {
                eventChannel.send(ActivityEvent.ActivitySaveError)
            }

            state = state.copy(isSaving = false)
        }
    }

    private fun listenToWatchActions() {
        watchConnector.messages
            .onEach { message ->
                when (message) {
                    MessagingAction.Finish -> onAction(ActivityAction.OnFinishClick, fromWatch = true)
                    MessagingAction.Pause -> onAction(ActivityAction.OnPauseClick, fromWatch = true)
                    MessagingAction.Resume -> onAction(ActivityAction.OnResumeClick, fromWatch = true)
                    MessagingAction.Start -> onAction(ActivityAction.OnStartClick, fromWatch = true)
                    MessagingAction.ConnectionRequest -> {
                        if (state.status.isActive) {
                            watchConnector.sendMessageToWatch(MessagingAction.Start)
                        }
                    }

                    else -> Unit
                }
            }.launchIn(viewModelScope)
    }

    private fun sendActionToWatch(action: ActivityAction) {
        viewModelScope.launch {
            val message = when (action) {
                ActivityAction.DiscardActivity -> MessagingAction.Finish
                ActivityAction.OnFinishClick -> MessagingAction.Finish
                ActivityAction.OnPauseClick -> MessagingAction.Pause
                ActivityAction.OnResumeClick -> MessagingAction.Resume
                ActivityAction.OnStartClick -> MessagingAction.Start
                else -> null
            }

            if (message == MessagingAction.Pause) {
                watchConnector.sendMessageToWatch(MessagingAction.WakeUpWatch)
            }

            message?.let {
                watchConnector.sendMessageToWatch(it)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        if (ActivityTrackerService.isActive.not()) {
            tracker.clear()

            applicationScope.launch {
                watchConnector.sendMessageToWatch(MessagingAction.CanNotTrack)
            }
        }
    }
}
