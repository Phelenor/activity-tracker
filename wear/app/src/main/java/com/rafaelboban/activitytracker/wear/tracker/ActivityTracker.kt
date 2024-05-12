@file:OptIn(ExperimentalCoroutinesApi::class)

package com.rafaelboban.activitytracker.wear.tracker

import com.rafaelboban.core.shared.connectivity.connectors.WatchToPhoneConnector
import com.rafaelboban.core.shared.connectivity.model.MessagingAction
import com.rafaelboban.core.shared.model.ActivityStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
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
    private val _isActive = MutableStateFlow(false)
    val isActive = _isActive.asStateFlow()

    private val _activityStatus = MutableStateFlow(ActivityStatus.NOT_STARTED)
    val activityStatus = _activityStatus.asStateFlow()

    private val _canTrack = MutableStateFlow(false)
    val canTrack = _canTrack.asStateFlow()

    private val _heartRate = MutableStateFlow(0)
    val heartRate = _heartRate.asStateFlow()

    val distanceMeters = phoneConnector.messages
        .filterIsInstance<MessagingAction.DistanceUpdate>()
        .map { it.distanceMeters }
        .stateIn(
            applicationScope,
            SharingStarted.Lazily,
            0
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
                    else -> Unit
                }
            }.launchIn(applicationScope)

        phoneConnector.connectedNode
            .filterNotNull()
            .onEach {
                exerciseTracker.prepareExercise()
            }.launchIn(applicationScope)

        canTrack.flatMapLatest { canTrack ->
            if (canTrack) {
                exerciseTracker.heartRate
            } else {
                flowOf()
            }
        }.onEach { heartRate ->
            phoneConnector.sendMessageToPhone(MessagingAction.HeartRateUpdate(heartRate))
            _heartRate.value = heartRate
        }.launchIn(applicationScope)
    }

    fun setIsActive(active: Boolean) {
        _isActive.value = active
    }

    fun setStatus(status: ActivityStatus) {
        _activityStatus.value = status
    }
}
