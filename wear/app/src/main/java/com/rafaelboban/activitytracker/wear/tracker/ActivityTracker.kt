@file:OptIn(ExperimentalCoroutinesApi::class)

package com.rafaelboban.activitytracker.wear.tracker

import com.rafaelboban.core.shared.connectivity.connectors.WatchToPhoneConnector
import com.rafaelboban.core.shared.connectivity.model.MessagingAction
import com.rafaelboban.core.shared.model.ActivityStatus
import com.rafaelboban.core.shared.model.ActivityType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration

class ActivityTracker(
    applicationScope: CoroutineScope,
    private val phoneConnector: WatchToPhoneConnector,
    private val exerciseTracker: HealthServicesExerciseTracker
) {
    private val _activityStatus = MutableStateFlow(ActivityStatus.NOT_STARTED)
    val activityStatus = _activityStatus.asStateFlow()

    private val _activityType = MutableStateFlow<ActivityType?>(null)
    val activityType = _activityType.asStateFlow()

    private val _canTrack = MutableStateFlow(false)
    val canTrack = _canTrack.asStateFlow()

    private val _heartRate = MutableStateFlow(0)
    val heartRate = _heartRate.asStateFlow()

    private val _calories = MutableStateFlow(0)
    val calories = _calories.asStateFlow()

    val distanceMeters = phoneConnector.messages
        .filterIsInstance<MessagingAction.DistanceUpdate>()
        .map { it.distanceMeters }
        .stateIn(
            applicationScope,
            SharingStarted.Lazily,
            0
        )

    val speed = phoneConnector.messages
        .filterIsInstance<MessagingAction.SpeedUpdate>()
        .map { it.speed }
        .stateIn(
            applicationScope,
            SharingStarted.Lazily,
            0f
        )

    val duration = phoneConnector.messages
        .filterIsInstance<MessagingAction.DurationUpdate>()
        .map { it.duration }
        .stateIn(
            applicationScope,
            SharingStarted.Lazily,
            Duration.ZERO
        )

    init {
        phoneConnector.messages
            .onEach { action ->
                when (action) {
                    is MessagingAction.CanTrack -> _canTrack.value = true
                    is MessagingAction.CanNotTrack -> _canTrack.value = false
                    is MessagingAction.SetActivityType -> _activityType.value = action.activityType
                    else -> Unit
                }
            }.launchIn(applicationScope)

        canTrack.combine(activityType) { canTrack, activityType ->
            if (canTrack && activityType != null) {
                exerciseTracker.prepareExercise(activityType.toExerciseType())
            }
        }

        canTrack.flatMapLatest { canTrack ->
            if (canTrack) {
                exerciseTracker.healthData
            } else {
                flowOf()
            }
        }.onEach { data ->
            data.heartRate?.let { heartRate ->
                phoneConnector.sendMessageToPhone(MessagingAction.HeartRateUpdate(heartRate))
                _heartRate.value = heartRate.heartRate
            }

            data.calories?.let { calories ->
                phoneConnector.sendMessageToPhone(MessagingAction.CaloriesUpdate(calories))
                _calories.value = calories
            }
        }.launchIn(applicationScope)
    }

    fun setStatus(status: ActivityStatus) {
        _activityStatus.value = status
    }

    fun reset() {
        setStatus(ActivityStatus.NOT_STARTED)

        _activityType.value = null
        _canTrack.value = false
        _heartRate.value = 0
    }
}
