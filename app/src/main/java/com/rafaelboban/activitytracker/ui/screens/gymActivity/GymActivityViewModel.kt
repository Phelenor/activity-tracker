@file:OptIn(ExperimentalCoroutinesApi::class)

package com.rafaelboban.activitytracker.ui.screens.gymActivity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.model.gym.GymEquipment
import com.rafaelboban.activitytracker.model.network.Activity
import com.rafaelboban.activitytracker.model.network.FetchStatus
import com.rafaelboban.activitytracker.network.repository.ActivityRepository
import com.rafaelboban.activitytracker.network.repository.GymRepository
import com.rafaelboban.activitytracker.network.ws.ActivityControlAction
import com.rafaelboban.activitytracker.service.ActivityTrackerService
import com.rafaelboban.activitytracker.tracking.GymActivityDataService
import com.rafaelboban.activitytracker.tracking.Timer
import com.rafaelboban.core.shared.model.ActivityStatus
import com.rafaelboban.core.shared.model.ActivityStatus.Companion.isActive
import com.skydoves.sandwich.onFailure
import com.skydoves.sandwich.onSuccess
import com.skydoves.sandwich.suspendOnFailure
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class GymActivityViewModel @Inject constructor(
    private val gymRepository: GymRepository,
    private val activityRepository: ActivityRepository,
    private val dataService: GymActivityDataService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val id: String = checkNotNull(savedStateHandle["id"])

    var state by mutableStateOf(GymActivityState())
        private set

    private val eventChannel = Channel<GymActivityEvent>()
    val events = eventChannel.receiveAsFlow()

    private val isTrackingActivity = snapshotFlow { state.status == ActivityStatus.IN_PROGRESS }

    init {
        getGymEquipment()
    }

    private fun getGymEquipment() {
        viewModelScope.launch {
            state = state.copy(gymEquipmentFetchStatus = FetchStatus.IN_PROGRESS)

            gymRepository.getEquipment(id).onSuccess {
                state = state.copy(gymEquipment = data, gymEquipmentFetchStatus = FetchStatus.SUCCESS)
                initializeDataConnection(data)
            }.onFailure {
                state = state.copy(gymEquipmentFetchStatus = FetchStatus.ERROR)
            }
        }
    }

    private fun initializeDataConnection(equipment: GymEquipment) {
        dataService.initialize(equipment)

        isTrackingActivity.flatMapLatest { isTracking ->
            if (isTracking) Timer.time() else flowOf()
        }.onEach { interval ->
            state = state.copy(duration = state.duration + interval)
        }.launchIn(viewModelScope)

        isTrackingActivity.flatMapLatest { isTracking ->
            if (isTracking) dataService.dataSnapshot else flowOf()
        }.filterNotNull().onEach { data ->
            state = state.copy(
                activityData = state.activityData.copy(
                    distanceMeters = data.distance,
                    heartRate = data.heartRate,
                    speed = data.speed,
                    avgSpeed = data.avgSpeed,
                    avgHeartRate = data.avgHeartRate,
                    elevationGain = data.elevationGain,
                    calories = data.calories,
                    maxSpeed = data.maxSpeed,
                    maxHeartRate = data.maxHeartRate
                )
            )
        }.launchIn(viewModelScope)

        dataService.controls
            .filterNotNull()
            .onEach { control ->
                onAction(
                    when (control) {
                        ActivityControlAction.START -> GymActivityAction.OnStartClick
                        ActivityControlAction.PAUSE -> GymActivityAction.OnPauseClick
                        ActivityControlAction.RESUME -> GymActivityAction.OnResumeClick
                        ActivityControlAction.FINISH -> GymActivityAction.OnFinishClick
                    }
                )
            }.launchIn(viewModelScope)
    }

    fun onAction(action: GymActivityAction) {
        when (action) {
            GymActivityAction.DiscardActivity -> {
                viewModelScope.launch {
                    eventChannel.trySend(GymActivityEvent.NavigateBack)
                }
            }

            GymActivityAction.DismissDialogs -> {
                state = state.copy(
                    showDiscardDialog = false,
                    showDoYouWantToSaveDialog = false
                )
            }

            GymActivityAction.OnBackClick -> {
                state = state.copy(showDiscardDialog = state.status.isActive)
            }

            GymActivityAction.RetryGymActivityFetch -> {
                getGymEquipment()
            }

            GymActivityAction.OnFinishClick -> {
                dataService.sendControlAction(ActivityControlAction.FINISH)

                val shorterThan30Seconds = state.duration < 30.seconds

                state = state.copy(
                    status = ActivityStatus.FINISHED,
                    endTimestamp = Instant.now().epochSecond,
                    isSaving = !shorterThan30Seconds,
                    showDoYouWantToSaveDialog = shorterThan30Seconds
                )

                if (state.isSaving) {
                    saveActivity()
                }
            }

            GymActivityAction.OnPauseClick -> {
                dataService.sendControlAction(ActivityControlAction.PAUSE)
                state = state.copy(status = ActivityStatus.PAUSED)
            }

            GymActivityAction.OnResumeClick -> {
                dataService.sendControlAction(ActivityControlAction.RESUME)
                state = state.copy(status = ActivityStatus.IN_PROGRESS)
            }

            GymActivityAction.OnStartClick -> {
                dataService.sendControlAction(ActivityControlAction.START)
                state = state.copy(
                    status = ActivityStatus.IN_PROGRESS,
                    startTimestamp = Instant.now().epochSecond
                )
            }

            GymActivityAction.SaveActivity -> {
                saveActivity()
            }
        }
    }

    private fun saveActivity() {
        val equipment = state.gymEquipment ?: run {
            state = state.copy(isSaving = false)
            return
        }

        viewModelScope.launch {
            val activity = Activity(
                activityType = equipment.activityType,
                startTimestamp = state.startTimestamp,
                endTimestamp = state.endTimestamp,
                durationSeconds = state.duration.inWholeSeconds,
                distanceMeters = state.activityData.distanceMeters,
                elevation = state.activityData.elevationGain,
                calories = state.activityData.calories,
                avgHeartRate = state.activityData.avgHeartRate,
                avgSpeedKmh = state.activityData.avgSpeed,
                maxHeartRate = state.activityData.maxHeartRate,
                maxSpeedKmh = state.activityData.maxSpeed,
                heartRateZoneDistribution = emptyMap(),
                goals = emptyList(),
                weather = null,
                isGymActivity = true
            )

            activityRepository.saveActivity(activity, byteArrayOf()).suspendOnFailure {
                eventChannel.send(GymActivityEvent.ActivitySaveError)
            }

            state = state.copy(isSaving = false)
        }
    }

    override fun onCleared() {
        super.onCleared()

        if (ActivityTrackerService.isActive.not()) {
            dataService.clear()
        }
    }
}
