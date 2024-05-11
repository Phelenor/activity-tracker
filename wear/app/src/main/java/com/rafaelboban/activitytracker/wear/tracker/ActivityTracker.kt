@file:OptIn(ExperimentalCoroutinesApi::class)

package com.rafaelboban.activitytracker.wear.tracker

import android.util.Log
import com.rafaelboban.core.shared.connectivity.connectors.WatchToPhoneConnector
import com.rafaelboban.core.shared.connectivity.model.MessagingAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
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
    private val _heartRate = MutableStateFlow(0)
    val heartRate = _heartRate.asStateFlow()

    private val _canTrack = MutableStateFlow(false)
    val canTrack = _canTrack.asStateFlow()

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

        exerciseTracker.heartRate
            .onEach { heartRate ->
                Log.d("MARIN", "69: sending hr=$heartRate")
                phoneConnector.sendMessageToPhone(MessagingAction.HeartRateUpdate(heartRate))
                _heartRate.value = heartRate
            }.launchIn(applicationScope)
    }
}
