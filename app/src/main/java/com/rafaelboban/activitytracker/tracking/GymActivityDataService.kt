package com.rafaelboban.activitytracker.tracking

import com.rafaelboban.activitytracker.model.gym.GymEquipment
import com.rafaelboban.activitytracker.network.ws.ActivityControlAction
import com.rafaelboban.activitytracker.network.ws.GymActivityMessage
import com.rafaelboban.activitytracker.network.ws.WebSocketClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject

class GymActivityDataService @Inject constructor(
    private val webSocketClient: WebSocketClient,
    private val applicationScope: CoroutineScope
) {
    private val _controls = MutableSharedFlow<ActivityControlAction>()
    val controls = _controls.asSharedFlow()

    private val _gymEquipment = MutableStateFlow<GymEquipment?>(null)
    val gymEquipment = _gymEquipment.asStateFlow()

    private val _dataSnapshot = MutableStateFlow<GymActivityMessage.DataSnapshot?>(null)
    val userData = _dataSnapshot.asStateFlow()

    val equipmentId: String
        get() = checkNotNull(_gymEquipment.value?.id)

    val isInitialized: Boolean
        get() = _gymEquipment.value != null

    private val jobs = mutableListOf<Job>()

    fun initialize(gymEquipment: GymEquipment) {
        this._gymEquipment.update { gymEquipment }

        cancelJobs()

        webSocketClient.connect("/ws/activity/gym/$equipmentId")
            .onEach { message ->
                Timber.tag("WebSocket").i("New message: $message")

                when (val activityMessage = Json.decodeFromString<GymActivityMessage>(message)) {
                    is GymActivityMessage.ControlAction -> {
                        applicationScope.launch {
                            _controls.emit(activityMessage.action)
                        }
                    }

                    is GymActivityMessage.DataSnapshot -> {
                        _dataSnapshot.update { activityMessage }
                    }
                }
            }.launchIn(applicationScope).also { jobs.add(it) }
    }

    fun sendControlAction(action: ActivityControlAction) {
        val message = GymActivityMessage.ControlAction(action)
        val json = Json.encodeToString<GymActivityMessage>(message)
        sendMessage(json)
    }

    private fun sendMessage(json: String) {
        webSocketClient.send("/ws/activity/gym/$equipmentId", json)
    }

    fun clear() {
        runCatching {
            webSocketClient.close("/ws/activity/gym/$equipmentId")
        }

        cancelJobs()
        _gymEquipment.update { null }
        _dataSnapshot.update { null }
    }

    private fun cancelJobs() {
        jobs.forEach { it.cancel() }
        jobs.clear()
    }
}
