package com.rafaelboban.activitytracker.ui.screens.groupActivity

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.model.network.Activity
import com.rafaelboban.activitytracker.model.network.ActivityWeatherInfo
import com.rafaelboban.activitytracker.model.network.FetchStatus
import com.rafaelboban.activitytracker.network.repository.ActivityRepository
import com.rafaelboban.activitytracker.network.repository.WeatherRepository
import com.rafaelboban.activitytracker.service.ActivityTrackerService
import com.rafaelboban.activitytracker.tracking.ActivityTracker
import com.rafaelboban.activitytracker.util.UserData
import com.rafaelboban.core.shared.connectivity.connectors.PhoneToWatchConnector
import com.rafaelboban.core.shared.connectivity.model.MessagingAction
import com.rafaelboban.core.shared.model.ActivityStatus
import com.rafaelboban.core.shared.model.ActivityStatus.Companion.isActive
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.shared.utils.DEFAULT_HEART_RATE_TRACKER_AGE
import com.rafaelboban.core.shared.utils.F
import com.rafaelboban.core.shared.utils.HeartRateZoneHelper
import com.skydoves.sandwich.message
import com.skydoves.sandwich.onFailure
import com.skydoves.sandwich.onSuccess
import com.skydoves.sandwich.suspendOnFailure
import dagger.hilt.android.lifecycle.HiltViewModel
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

/**
 * Should inherit functionality from [com.rafaelboban.activitytracker.ui.screens.activity.ActivityViewModel]
 * But there is little time for these adaptations as the deadline is close
 */
@HiltViewModel
class GroupActivityViewModel @Inject constructor(
    private val applicationScope: CoroutineScope,
    private val tracker: ActivityTracker,
    private val watchConnector: PhoneToWatchConnector,
    private val activityRepository: ActivityRepository,
    private val weatherRepository: WeatherRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val id: String = checkNotNull(savedStateHandle["id"])
    private val currentUser = checkNotNull(UserData.user)

    var state by mutableStateOf(GroupActivityState())
        private set

    private val eventChannel = Channel<GroupActivityEvent>()
    val events = eventChannel.receiveAsFlow()

    private var weatherUpdateJob: Job? = null

    init {
        getGroupActivity()
    }

    private fun getGroupActivity() {
        viewModelScope.launch {
            state = state.copy(groupActivityFetchStatus = FetchStatus.IN_PROGRESS)

            activityRepository.getGroupActivity(id).onSuccess {
                state = state.copy(groupActivity = data, isActivityOwner = currentUser.id == data.userOwnerId, groupActivityFetchStatus = FetchStatus.SUCCESS)
                initTracker(data.activityType, state.isActivityOwner)
            }.onFailure {
                state = state.copy(groupActivityFetchStatus = FetchStatus.ERROR)
            }
        }
    }

    private fun initTracker(type: ActivityType, isActivityOwner: Boolean) {
        tracker.startTrackingLocation(type, isGroupActivity = true, isGroupActivityOwner = isActivityOwner)

        viewModelScope.launch {
            watchConnector.sendMessageToWatch(MessagingAction.GroupActivityMarker(isActivityOwner))
        }

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

        listenToWatchActions()
        startWeatherUpdates()
    }

    fun onAction(action: GroupActivityAction, fromWatch: Boolean = false) {
        if (!fromWatch) {
            sendActionToWatch(action)
        }

        when (action) {
            GroupActivityAction.OnStartClick -> {
                state = state.copy(status = ActivityStatus.IN_PROGRESS)
                tracker.setActivityStatus(ActivityStatus.IN_PROGRESS)
                tracker.setIsTrackingActivity(true)
            }

            GroupActivityAction.OnPauseClick -> {
                state = state.copy(status = ActivityStatus.PAUSED)
                tracker.setActivityStatus(ActivityStatus.PAUSED)
                tracker.setIsTrackingActivity(false)
            }

            GroupActivityAction.OnResumeClick -> {
                state = state.copy(status = ActivityStatus.IN_PROGRESS)
                tracker.setActivityStatus(ActivityStatus.IN_PROGRESS)
                tracker.setIsTrackingActivity(true)
            }

            GroupActivityAction.OnFinishClick -> {
                val shorterThan30Seconds = state.duration < 30.seconds
                val hasAtLeast5Locations = state.activityData.locations.flatten().size >= 5

                state = state.copy(status = ActivityStatus.FINISHED, isSaving = !shorterThan30Seconds && hasAtLeast5Locations, showDoYouWantToSaveDialog = shorterThan30Seconds && hasAtLeast5Locations)
                tracker.setActivityStatus(ActivityStatus.FINISHED)
                tracker.setIsTrackingActivity(false)
                tracker.stopTrackingLocation()
            }

            GroupActivityAction.OnBackClick -> {
                state = state.copy(showDiscardDialog = state.status.isActive)
            }

            GroupActivityAction.DismissDialogs -> {
                state = state.copy(
                    showDiscardDialog = false,
                    showSelectMapTypeDialog = false,
                    showDoYouWantToSaveDialog = false,
                    showShareDialog = false
                )
            }

            GroupActivityAction.OnCameraLockToggle -> {
                state = state.copy(mapCameraLocked = !state.mapCameraLocked)
            }

            GroupActivityAction.OnOpenSelectMapType -> {
                state = state.copy(showSelectMapTypeDialog = true)
            }

            GroupActivityAction.OnShareClick -> {
                state = state.copy(showShareDialog = true)
            }

            GroupActivityAction.DiscardActivity -> {
                viewModelScope.launch {
                    eventChannel.trySend(GroupActivityEvent.NavigateBack)
                }
            }

            GroupActivityAction.OnReloadWeather -> {
                startWeatherUpdates()
            }

            is GroupActivityAction.OnTabChanged -> {
                state = state.copy(selectedBottomSheetTab = action.tab)
            }

            GroupActivityAction.SaveActivity -> {
                state = state.copy(isSaving = true)
            }

            is GroupActivityAction.MapSnapshotDone -> {
                saveActivity(action.stream)
            }

            GroupActivityAction.RetryGroupActivityFetch -> {
                getGroupActivity()
            }

            is GroupActivityAction.OnSelectMapType -> {
                state = state.copy(mapType = action.type)
            }
        }
    }

    private fun saveActivity(mapSnapshot: ByteArray) {
        viewModelScope.launch {
            val groupActivity = checkNotNull(state.groupActivity)
            val zoneDistribution = HeartRateZoneHelper.calculateHeartRateZoneDistribution(state.activityData.heartRatePoints, UserData.user?.age ?: DEFAULT_HEART_RATE_TRACKER_AGE, state.duration)
            val activity = Activity(
                activityType = groupActivity.activityType,
                groupActivityId = groupActivity.id,
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
                goals = emptyList(),
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
                eventChannel.send(GroupActivityEvent.ActivitySaveError)
            }

            state = state.copy(isSaving = false)
        }
    }

    private fun listenToWatchActions() {
        watchConnector.messages
            .onEach { message ->
                when (message) {
                    MessagingAction.Finish -> onAction(GroupActivityAction.OnFinishClick, fromWatch = true)
                    MessagingAction.Pause -> onAction(GroupActivityAction.OnPauseClick, fromWatch = true)
                    MessagingAction.Resume -> onAction(GroupActivityAction.OnResumeClick, fromWatch = true)
                    MessagingAction.Start -> onAction(GroupActivityAction.OnStartClick, fromWatch = true)
                    MessagingAction.ConnectionRequest -> {
                        if (state.status.isActive) {
                            watchConnector.sendMessageToWatch(MessagingAction.Start)
                        }
                    }

                    else -> Unit
                }
            }.launchIn(viewModelScope)
    }

    private fun sendActionToWatch(action: GroupActivityAction) {
        viewModelScope.launch {
            val message = when (action) {
                GroupActivityAction.DiscardActivity -> MessagingAction.Finish
                GroupActivityAction.OnFinishClick -> MessagingAction.Finish
                GroupActivityAction.OnPauseClick -> MessagingAction.Pause
                GroupActivityAction.OnResumeClick -> MessagingAction.Resume
                GroupActivityAction.OnStartClick -> MessagingAction.Start
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
                    Log.d("MARIN", "302: startWeatherUpdates ${message()}")
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
