@file:OptIn(FlowPreview::class)

package com.rafaelboban.activitytracker.tracking

import com.rafaelboban.activitytracker.network.ws.ActivityMessage
import com.rafaelboban.activitytracker.network.ws.WebSocketClient
import com.rafaelboban.core.shared.utils.F
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class GroupActivityDataService @Inject constructor(
    private val tracker: ActivityTracker,
    private val webSocketClient: WebSocketClient,
    private val applicationScope: CoroutineScope
) {

    private val jobs = mutableListOf<Job>()

    fun initialize(id: String) {
        cancelJobs()

        webSocketClient.connect("/ws/activity/$id")
            .onEach { message ->
                // TODO: emit to new users data flow / status update flow
                Timber.tag("MARIN").d("New message: $message")
            }.launchIn(applicationScope).also { jobs.add(it) }

        tracker.currentLocation.filterNotNull()
            .combine(tracker.data) { location, data ->
                ActivityMessage.DataUpdate(
                    lat = location.location.latitude.F,
                    long = location.location.longitude.F,
                    speed = data.speed,
                    distance = data.distanceMeters,
                    heartRate = data.currentHeartRate?.heartRate ?: 0
                )
            }
            .distinctUntilChanged()
            .sample(3.seconds)
            .onEach { message ->
                webSocketClient.send("/ws/activity/$id", Json.encodeToString(message))
            }.launchIn(applicationScope).also { jobs.add(it) }
    }

    fun clear(id: String) {
        cancelJobs()

        webSocketClient.close("/ws/activity/$id")
    }

    private fun cancelJobs() {
        jobs.forEach { it.cancel() }
        jobs.clear()
    }
}
