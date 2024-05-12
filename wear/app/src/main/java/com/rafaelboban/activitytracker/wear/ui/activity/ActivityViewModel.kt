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
import com.rafaelboban.activitytracker.wear.tracker.toUiText
import com.rafaelboban.core.shared.connectivity.connectors.WatchToPhoneConnector
import com.rafaelboban.core.shared.connectivity.model.MessagingAction
import com.rafaelboban.core.shared.model.ActivityStatus
import com.rafaelboban.core.shared.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val phoneConnector: WatchToPhoneConnector,
    private val exerciseTracker: HealthServicesExerciseTracker,
    private val activityTracker: ActivityTracker
) : ViewModel() {

    var state by mutableStateOf(
        ActivityState(
            isActive = ActivityTrackerService.isActive && activityTracker.isActive.value,
            activityStatus = activityTracker.activityStatus.value,
            canTrack = ActivityTrackerService.isActive
        )
    )
        private set

    private val canTrackHeartRate = snapshotFlow {
        state.canTrackHeartRate && state.canTrack
    }.stateIn(viewModelScope, SharingStarted.Lazily, state.canTrackHeartRate && state.canTrack)

    private val isActive = snapshotFlow {
        state.isActive && state.canTrack && state.isConnectedPhoneNearby
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val activityStatus = snapshotFlow {
        state.activityStatus
    }.stateIn(viewModelScope, SharingStarted.Lazily, state.activityStatus)

    private val eventChannel = Channel<ActivityEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            val isHeartRateSupported = exerciseTracker.isHeartRateTrackingSupported()

            state = state.copy(
                canTrackHeartRate = isHeartRateSupported
            )
        }

        phoneConnector.connectedNode
            .filterNotNull()
            .onEach { node ->
                state = state.copy(isConnectedPhoneNearby = node.isNearby)
            }.combine(isActive) { _, isActive ->
                if (isActive.not()) {
                    phoneConnector.sendMessageToPhone(MessagingAction.ConnectionRequest)
                }
            }.launchIn(viewModelScope)

        activityTracker.canTrack.onEach { canTrack ->
            state = state.copy(canTrack = canTrack)
        }.launchIn(viewModelScope)

        canTrackHeartRate.flatMapLatest { canTrack ->
            if (canTrack) activityTracker.heartRate else flowOf()
        }.onEach { heartRate ->
            state = state.copy(heartRate = heartRate)
        }.launchIn(viewModelScope)

        isActive.onEach { isActive ->
            activityTracker.setIsActive(isActive)
        }.launchIn(viewModelScope)

        activityStatus.onEach { status ->
            val previousStatus = activityTracker.activityStatus.value

            val result = when (status) {
                ActivityStatus.IN_PROGRESS -> if (previousStatus == ActivityStatus.NOT_STARTED) exerciseTracker.startExercise() else exerciseTracker.resumeExercise()
                ActivityStatus.PAUSED -> exerciseTracker.pauseExercise()
                ActivityStatus.FINISHED -> exerciseTracker.stopExercise()
                ActivityStatus.NOT_STARTED -> Result.Success(Unit)
            }

            if (result is Result.Error) {
                result.error.toUiText()?.let {
                    eventChannel.send(ActivityEvent.Error(it))
                }
            }

            activityTracker.setStatus(status)
        }.launchIn(viewModelScope)

        activityTracker.distanceMeters
            .onEach { distanceMeters ->
                state = state.copy(distanceMeters = distanceMeters)
            }.launchIn(viewModelScope)

        activityTracker.duration
            .onEach { duration ->
                state = state.copy(duration = duration)
            }.launchIn(viewModelScope)

        listenToPhoneMessages()
    }

    fun onAction(action: ActivityAction, fromPhone: Boolean = false) {
        if (fromPhone.not()) {
            sendActionToPhone(action)
        }

        when (action) {
            ActivityAction.GrantBodySensorsPermission -> {
                viewModelScope.launch {
                    val canTrackHeartRate = exerciseTracker.isHeartRateTrackingSupported()
                    state = state.copy(canTrackHeartRate = canTrackHeartRate)
                }
            }

            ActivityAction.OpenAppOnPhone -> {
                viewModelScope.launch {
                    phoneConnector.sendMessageToPhone(MessagingAction.OpenAppOnPhone)
                }
            }

            ActivityAction.OnFinishClick -> {
                state = state.copy(
                    isActive = false,
                    activityStatus = ActivityStatus.FINISHED
                )
            }

            ActivityAction.OnPauseClick -> state = state.copy(
                activityStatus = ActivityStatus.PAUSED
            )

            ActivityAction.OnResumeClick -> state = state.copy(
                activityStatus = ActivityStatus.IN_PROGRESS
            )

            ActivityAction.OnStartClick -> state = state.copy(
                isActive = true,
                activityStatus = ActivityStatus.IN_PROGRESS
            )
        }
    }

    private fun listenToPhoneMessages() {
        phoneConnector.messages
            .onEach { message ->
                when (message) {
                    MessagingAction.Start -> {
                        if (state.canTrack) {
                            state = state.copy(
                                isActive = true,
                                activityStatus = ActivityStatus.IN_PROGRESS
                            )
                        }
                    }

                    MessagingAction.Pause -> {
                        if (state.canTrack) {
                            state = state.copy(
                                activityStatus = ActivityStatus.PAUSED
                            )
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
}
