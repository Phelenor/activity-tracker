@file:OptIn(FlowPreview::class)

package com.rafaelboban.activitytracker.wear.ui.activity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.wear.service.ActivityTrackerService
import com.rafaelboban.activitytracker.wear.tracker.ActivityTracker
import com.rafaelboban.activitytracker.wear.tracker.HealthServicesExerciseTracker
import com.rafaelboban.activitytracker.wear.tracker.toExerciseType
import com.rafaelboban.activitytracker.wear.tracker.toUiText
import com.rafaelboban.core.shared.connectivity.connectors.WatchToPhoneConnector
import com.rafaelboban.core.shared.connectivity.model.MessagingAction
import com.rafaelboban.core.shared.model.ActivityStatus
import com.rafaelboban.core.shared.model.ActivityStatus.Companion.isActive
import com.rafaelboban.core.shared.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val activityTracker: ActivityTracker,
    private val exerciseTracker: HealthServicesExerciseTracker,
    private val phoneConnector: WatchToPhoneConnector
) : ViewModel() {

    var state by mutableStateOf(
        ActivityState(
            activityStatus = activityTracker.activityStatus.value,
            activityType = activityTracker.activityType.value,
            canTrack = ActivityTrackerService.isActive
        )
    )
        private set

    private val activityStatus = snapshotFlow {
        state.activityStatus
    }.stateIn(viewModelScope, SharingStarted.Lazily, state.activityStatus)

    private val eventChannel = Channel<ActivityEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        checkSupportedTrackingTypes()

        phoneConnector.connectedNode
            .filterNotNull()
            .onEach { node ->
                state = state.copy(isConnectedPhoneNearby = node.isNearby)
            }.combine(activityStatus) { _, status ->
                if (status.isActive.not() && status != ActivityStatus.FINISHED) {
                    phoneConnector.sendMessageToPhone(MessagingAction.ConnectionRequest)
                }
            }.launchIn(viewModelScope)

        activityTracker.canTrack.onEach { canTrack ->
            state = state.copy(canTrack = canTrack)
        }.launchIn(viewModelScope)

        activityTracker.activityType.onEach { type ->
            state = state.copy(activityType = type)
        }.launchIn(viewModelScope)

        activityStatus.onEach { status ->
            val previousStatus = activityTracker.activityStatus.value

            val result = when (status) {
                ActivityStatus.NOT_STARTED -> Result.Success(Unit)
                ActivityStatus.PAUSED -> exerciseTracker.pauseExercise()
                ActivityStatus.FINISHED -> exerciseTracker.stopExercise()
                ActivityStatus.IN_PROGRESS -> {
                    if (previousStatus == ActivityStatus.NOT_STARTED) {
                        val exerciseType = state.activityType?.toExerciseType() ?: return@onEach
                        exerciseTracker.startExercise(exerciseType)
                    } else {
                        exerciseTracker.resumeExercise()
                    }
                }
            }

            if (result is Result.Error) {
                result.error.toUiText()?.let {
                    eventChannel.send(ActivityEvent.Error(it))
                }
            }

            activityTracker.setStatus(status)
        }.launchIn(viewModelScope)

        val isInAmbientMode = snapshotFlow { state.isInAmbientMode }

        isInAmbientMode.flatMapLatest { inAmbientMode ->
            if (inAmbientMode) {
                activityTracker.heartRate.sample(10.seconds)
            } else {
                activityTracker.heartRate
            }
        }.onEach { heartRate ->
            state = state.copy(heartRate = heartRate)
        }.launchIn(viewModelScope)

        isInAmbientMode.flatMapLatest { inAmbientMode ->
            if (inAmbientMode) {
                activityTracker.calories.sample(10.seconds)
            } else {
                activityTracker.calories
            }
        }.onEach { calories ->
            state = state.copy(totalCaloriesBurned = calories)
        }.launchIn(viewModelScope)

        isInAmbientMode.flatMapLatest { inAmbientMode ->
            if (inAmbientMode) {
                activityTracker.distanceMeters.sample(10.seconds)
            } else {
                activityTracker.distanceMeters
            }
        }.onEach { distanceMeters ->
            state = state.copy(distanceMeters = distanceMeters)
        }.launchIn(viewModelScope)

        isInAmbientMode.flatMapLatest { inAmbientMode ->
            if (inAmbientMode) {
                activityTracker.speed.sample(10.seconds)
            } else {
                activityTracker.speed
            }
        }.onEach { speed ->
            state = state.copy(speed = speed)
        }.launchIn(viewModelScope)

        isInAmbientMode.flatMapLatest { inAmbientMode ->
            if (inAmbientMode) {
                activityTracker.duration.sample(10.seconds)
            } else {
                activityTracker.duration
            }
        }.onEach { duration ->
            state = state.copy(duration = duration)
        }.launchIn(viewModelScope)

//        canTrackHeartRate.flatMapLatest { canTrack ->
//            if (canTrack) activityTracker.heartRate else flowOf()
//        }.onEach { heartRate ->
//            state = state.copy(heartRate = heartRate)
//        }.launchIn(viewModelScope)
//
//        activityTracker.distanceMeters
//            .onEach { distanceMeters ->
//                state = state.copy(distanceMeters = distanceMeters)
//            }.launchIn(viewModelScope)
//
//        activityTracker.duration
//            .onEach { duration ->
//                state = state.copy(duration = duration)
//            }.launchIn(viewModelScope)

        listenToPhoneMessages()
    }

    fun onAction(action: ActivityAction, fromPhone: Boolean = false) {
        if (fromPhone.not()) {
            sendActionToPhone(action)
        }

        when (action) {
            ActivityAction.OnFinishClick -> state = state.copy(activityStatus = ActivityStatus.FINISHED)
            ActivityAction.OnPauseClick -> state = state.copy(activityStatus = ActivityStatus.PAUSED)
            ActivityAction.OnResumeClick -> state = state.copy(activityStatus = ActivityStatus.IN_PROGRESS)
            ActivityAction.OnStartClick -> state = state.copy(activityStatus = ActivityStatus.IN_PROGRESS)
            ActivityAction.OnExitAmbientMode -> state = state.copy(isInAmbientMode = false)
            ActivityAction.GrantBodySensorsPermission -> checkSupportedTrackingTypes()
            ActivityAction.OpenAppOnPhone -> {
                viewModelScope.launch {
                    phoneConnector.sendMessageToPhone(MessagingAction.OpenAppOnPhone)
                }
            }

            is ActivityAction.OnEnterAmbientMode -> state = state.copy(
                isInAmbientMode = true,
                isBurnInProtectionRequired = action.isBurnInProtectionRequired
            )
        }
    }

    private fun listenToPhoneMessages() {
        phoneConnector.messages
            .onEach { message ->
                when (message) {
                    MessagingAction.Start -> {
                        if (state.canTrack) {
                            state = state.copy(activityStatus = ActivityStatus.IN_PROGRESS)
                        }
                    }

                    MessagingAction.Pause -> {
                        if (state.canTrack) {
                            state = state.copy(activityStatus = ActivityStatus.PAUSED)
                        }
                    }

                    MessagingAction.Resume -> {
                        if (state.canTrack) {
                            state = state.copy(
                                activityStatus = ActivityStatus.IN_PROGRESS
                            )
                        }
                    }

                    MessagingAction.Finish -> {
                        onAction(ActivityAction.OnFinishClick, fromPhone = true)
                    }

                    MessagingAction.CanTrack -> {
                        state = state.copy(canTrack = true)
                    }

                    MessagingAction.CanNotTrack -> {
                        state = ActivityState(
                            canTrackHeartRate = state.canTrackHeartRate,
                            isConnectedPhoneNearby = state.isConnectedPhoneNearby
                        )

                        activityTracker.reset()
                    }

                    else -> Unit
                }
            }.launchIn(viewModelScope)
    }

    private fun sendActionToPhone(action: ActivityAction) {
        viewModelScope.launch {
            val message = when (action) {
                is ActivityAction.OnFinishClick -> MessagingAction.Finish
                is ActivityAction.OnStartClick -> MessagingAction.Start
                is ActivityAction.OnResumeClick -> MessagingAction.Resume
                is ActivityAction.OnPauseClick -> MessagingAction.Pause
                else -> null
            }

            message?.let {
                phoneConnector.sendMessageToPhone(it)
            }
        }
    }

    private fun checkSupportedTrackingTypes() {
        viewModelScope.launch {
            val (isHeartRateSupported, isCalorieTrackingSupported) = listOf(
                async { exerciseTracker.isHeartRateTrackingSupported() },
                async { exerciseTracker.isCalorieTrackingSupported() }
            ).awaitAll()

            state = state.copy(
                canTrackHeartRate = isHeartRateSupported,
                canTrackCalories = isCalorieTrackingSupported
            )
        }
    }
}
