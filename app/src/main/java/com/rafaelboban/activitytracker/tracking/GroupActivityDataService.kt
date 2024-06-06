@file:OptIn(FlowPreview::class)

package com.rafaelboban.activitytracker.tracking

import com.rafaelboban.activitytracker.model.network.GroupActivity
import com.rafaelboban.activitytracker.network.ws.ActivityControlAction
import com.rafaelboban.activitytracker.network.ws.ActivityMessage
import com.rafaelboban.activitytracker.network.ws.WebSocketClient
import com.rafaelboban.activitytracker.util.UserData
import com.rafaelboban.core.shared.utils.F
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class GroupActivityDataService @Inject constructor(
    private val tracker: ActivityTracker,
    private val webSocketClient: WebSocketClient,
    private val applicationScope: CoroutineScope
) {

    private val _controls = MutableSharedFlow<ActivityControlAction>()
    val controls = _controls.asSharedFlow()

    private val _groupActivity = MutableStateFlow<GroupActivity?>(null)
    val groupActivity = _groupActivity.asStateFlow()

    private val _userData = MutableStateFlow<Map<String, ActivityMessage.UserDataSnapshot>>(emptyMap())
    val userData = _userData.asStateFlow()

    val activityId: String
        get() = checkNotNull(_groupActivity.value?.id)

    val isInitialized: Boolean
        get() = _groupActivity.value != null

    private val jobs = mutableListOf<Job>()

    fun initialize(groupActivity: GroupActivity) {
        this._groupActivity.update { groupActivity }

        cancelJobs()

        webSocketClient.connect("/ws/activity/$activityId")
            .onEach { message ->
                Timber.tag("WebSocket").i("New message: $message")

                when (val activityMessage = Json.decodeFromString<ActivityMessage>(message)) {
                    is ActivityMessage.ControlAction -> {
                        applicationScope.launch {
                            _controls.emit(activityMessage.action)
                        }
                    }

                    is ActivityMessage.UserDataSnapshot -> {
                        _userData.update { map ->
                            map.toMutableMap().apply {
                                put(activityMessage.userId, activityMessage)
                            }
                        }
                    }

                    is ActivityMessage.GroupActivityUpdate -> {
                        _groupActivity.update { activityMessage.activity }

                        _userData.update { data ->
                            data.toMutableMap().apply {
                                forEach { (id, snapshot) ->
                                    put(id, snapshot.copy(showOnMap = snapshot.duration != null && (id in activityMessage.activity.connectedUsers || id in activityMessage.activity.activeUsers)))
                                }
                            }
                        }
                    }

                    is ActivityMessage.UserFinish -> {
                        _userData.update { map ->
                            map[activityMessage.userId]?.let { user ->
                                map.toMutableMap().apply {
                                    put(activityMessage.userId, user.copy(duration = activityMessage.durationSeconds.toDuration(DurationUnit.SECONDS)))
                                }
                            } ?: run {
                                map
                            }
                        }
                    }
                }
            }.launchIn(applicationScope).also { jobs.add(it) }

        tracker.currentLocation.filterNotNull()
            .combine(tracker.data) { location, data ->
                ActivityMessage.UserDataSnapshot(
                    userId = UserData.requireUser().id,
                    userDisplayName = UserData.requireUser().displayName,
                    userImageUrl = UserData.requireUser().imageUrl,
                    lat = location.location.latitude.F,
                    long = location.location.longitude.F,
                    speed = data.speed,
                    distance = data.distanceMeters,
                    heartRate = data.currentHeartRate?.heartRate ?: 0
                )
            }
            .distinctUntilChanged()
            .sample(2.seconds)
            .onEach { message ->
                webSocketClient.send("/ws/activity/$activityId", Json.encodeToString<ActivityMessage>(message))
            }.launchIn(applicationScope).also { jobs.add(it) }
    }

    fun broadcastControlAction(action: ActivityControlAction) {
        val message = ActivityMessage.ControlAction(action)
        val json = Json.encodeToString<ActivityMessage>(message)
        sendMessage(json)
    }

    fun broadcastFinishMessage() {
        val message = ActivityMessage.UserFinish(UserData.requireUser().id, tracker.duration.value.inWholeSeconds.toInt())
        val json = Json.encodeToString<ActivityMessage>(message)
        sendMessage(json)
    }

    private fun sendMessage(json: String) {
        webSocketClient.send("/ws/activity/$activityId", json)
    }

    fun clear() {
        runCatching {
            webSocketClient.close("/ws/activity/$activityId")
        }

        cancelJobs()
        _groupActivity.update { null }
        _userData.update { emptyMap() }
    }

    private fun cancelJobs() {
        jobs.forEach { it.cancel() }
        jobs.clear()
    }
}
