package com.rafaelboban.activitytracker.ui.screens.activity

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.di.PreferencesStandard
import com.rafaelboban.activitytracker.network.model.goals.PREFERENCE_SHOW_GOALS_REMINDER
import com.rafaelboban.activitytracker.network.repository.WeatherRepository
import com.rafaelboban.activitytracker.service.ActivityTrackerService
import com.rafaelboban.activitytracker.tracking.ActivityTracker
import com.rafaelboban.activitytracker.util.edit
import com.rafaelboban.core.shared.connectivity.connectors.PhoneToWatchConnector
import com.rafaelboban.core.shared.connectivity.model.MessagingAction
import com.rafaelboban.core.shared.model.ActivityStatus
import com.rafaelboban.core.shared.model.ActivityStatus.Companion.isActive
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.shared.utils.F
import com.skydoves.sandwich.onFailure
import com.skydoves.sandwich.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val applicationScope: CoroutineScope,
    private val tracker: ActivityTracker,
    private val watchConnector: PhoneToWatchConnector,
    private val weatherRepository: WeatherRepository,
    @PreferencesStandard private val preferences: SharedPreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val type = checkNotNull(savedStateHandle.get<Int>("activityTypeOrdinal")).let { ordinal -> ActivityType.entries[ordinal] }

    var state by mutableStateOf(ActivityState(status = tracker.status.value, type = type))
        private set

    private val eventChannel = Channel<ActivityEvent>()
    val events = eventChannel.receiveAsFlow()

    private var weatherUpdateJob: Job? = null

    init {
        tracker.startTrackingLocation(type)

        tracker.currentLocation.onEach { currentLocation ->
            state = state.copy(currentLocation = currentLocation?.location)
        }.launchIn(viewModelScope)

        tracker.data.onEach { data ->
            state = state.copy(
                activityData = data,
                maxSpeed = max(state.maxSpeed, data.speed).coerceAtMost(state.activityData.speed)
            )
        }.launchIn(viewModelScope)

        tracker.duration.onEach { duration ->
            state = state.copy(duration = duration)
        }.launchIn(viewModelScope)

        tracker.goals.onEach { goals ->
            state = state.copy(goals = goals.toImmutableList())
        }

        if (preferences.getBoolean(PREFERENCE_SHOW_GOALS_REMINDER, true)) {
            viewModelScope.launch {
                delay(2.seconds)
                state = state.copy(showSetGoalsDialog = true)
            }
        }

        listenToWatchActions()
        startWeatherUpdates()
    }

    fun onAction(action: ActivityAction, fromWatch: Boolean = false) {
        if (!fromWatch) {
            sendActionToWatch(action)
        }

        when (action) {
            ActivityAction.OnStartClick -> {
                state = state.copy(status = ActivityStatus.IN_PROGRESS)
                tracker.setIsTrackingActivity(true)
                tracker.setActivityStatus(ActivityStatus.IN_PROGRESS)
            }

            ActivityAction.OnPauseClick -> {
                state = state.copy(status = ActivityStatus.PAUSED)
                tracker.setIsTrackingActivity(false)
                tracker.setActivityStatus(ActivityStatus.PAUSED)
            }

            ActivityAction.OnResumeClick -> {
                state = state.copy(status = ActivityStatus.IN_PROGRESS)
                tracker.setIsTrackingActivity(true)
                tracker.setActivityStatus(ActivityStatus.IN_PROGRESS)
            }

            ActivityAction.OnFinishClick -> {
                state = state.copy(status = ActivityStatus.FINISHED)
                tracker.setIsTrackingActivity(false)
                tracker.setActivityStatus(ActivityStatus.FINISHED)
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
                    showSetGoalsDialog = false
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
                startWeatherUpdates()
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

            is ActivityAction.DismissGoalsDialog -> {
                state = state.copy(showSetGoalsDialog = false)

                if (action.doNotShowAgain) {
                    preferences.edit { putBoolean(PREFERENCE_SHOW_GOALS_REMINDER, false) }
                }
            }

            is ActivityAction.OnSelectMapType -> {
                state = state.copy(mapType = action.type)
            }
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

    private fun startWeatherUpdates() {
        state = state.copy(weather = null)

        weatherUpdateJob?.cancel()
        weatherUpdateJob = viewModelScope.launch {
            var canRetry = true
            var shouldRetry = false

            while (isActive) {
                var location = state.currentLocation
                var lockCount = 0

                while (location == null) {
                    delay(1.seconds * (lockCount + 1))
                    location = state.currentLocation
                    lockCount++
                }

                state = state.copy(isWeatherLoading = true)

                weatherRepository.getWeatherData(location.latitude.F, location.longitude.F).onSuccess {
                    state = state.copy(weather = data.copy(hourly = data.hourly.take(5)), isWeatherLoading = false)
                    canRetry = true
                }.onFailure {
                    state = state.copy(isWeatherLoading = false)
                    shouldRetry = canRetry
                    canRetry = false
                }

                if (shouldRetry) {
                    shouldRetry = false
                    continue
                }

                delay(15.minutes)
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
