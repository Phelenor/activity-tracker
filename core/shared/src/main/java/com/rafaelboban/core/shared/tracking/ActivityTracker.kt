@file:OptIn(ExperimentalCoroutinesApi::class)

package com.rafaelboban.core.shared.tracking

import com.rafaelboban.core.shared.connectivity.connectors.PhoneToWatchConnector
import com.rafaelboban.core.shared.connectivity.model.MessagingAction
import com.rafaelboban.core.shared.model.ActivityData
import com.rafaelboban.core.shared.model.ActivityStatus
import com.rafaelboban.core.shared.model.location.LocationTimestamp
import com.rafaelboban.core.shared.utils.currentSpeed
import com.rafaelboban.core.shared.utils.distanceSequenceMeters
import com.rafaelboban.core.shared.utils.replaceLastSublist
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.zip
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class ActivityTracker(
    applicationScope: CoroutineScope,
    private val locationObserver: LocationObserver,
    private val watchConnector: PhoneToWatchConnector
) {

    private val _activityData = MutableStateFlow(ActivityData())
    val activityData = _activityData.asStateFlow()

    private val _duration = MutableStateFlow(Duration.ZERO)
    val duration = _duration.asStateFlow()

    private val _activityStatus = MutableStateFlow(ActivityStatus.NOT_STARTED)
    val activityStatus = _activityStatus.asStateFlow()

    private val isTrackingActivity = MutableStateFlow(false)
    private val isTrackingLocation = MutableStateFlow(false)

    val currentLocation = isTrackingLocation
        .flatMapLatest { isObservingLocation ->
            if (isObservingLocation) {
                locationObserver.observeLocation((1.5).seconds.inWholeMilliseconds)
            } else {
                flowOf()
            }
        }
        .stateIn(
            applicationScope,
            SharingStarted.Lazily,
            null
        )

    private val heartRates = watchConnector.messages
        .filterIsInstance<MessagingAction.HeartRateUpdate>()
        .map { it.heartRate }
        .runningFold(initial = emptyList<Int>()) { currentHeartRates, newHeartRate ->
            currentHeartRates + newHeartRate
        }.stateIn(
            applicationScope,
            SharingStarted.Lazily,
            emptyList()
        )

    init {
        isTrackingActivity.onEach { isTracking ->
            if (isTracking.not()) {
                _activityData.update { data ->
                    data.copy(locations = (data.locations + listOf(persistentListOf())).toImmutableList())
                }
            }
        }.flatMapLatest { isTracking ->
            if (isTracking) Timer.time() else flowOf()
        }.onEach { interval ->
            _duration.update { it + interval }
        }.launchIn(applicationScope)

        currentLocation
            .filterNotNull()
            .combineTransform(isTrackingActivity) { location, isTracking ->
                if (isTracking) {
                    emit(location)
                }
            }.zip(_duration) { location, duration ->
                LocationTimestamp(
                    location = location,
                    durationTimestamp = duration
                )
            }.combine(heartRates) { location, heartRates ->
                _activityData.update { data ->
                    val currentSpeed = location.location.speed ?: Float.MAX_VALUE
                    val distanceFromLastLocation = data.locations.lastOrNull()?.lastOrNull()?.latLong?.distanceTo(location.latLong) ?: Float.MAX_VALUE
                    val shouldSaveLocation = currentSpeed > 0.8 && distanceFromLastLocation > 1

                    val currentLocationSequence = when {
                        data.locations.isEmpty() -> listOf(location)
                        !shouldSaveLocation -> data.locations.last()
                        else -> data.locations.last() + location
                    }

                    ActivityData(
                        locations = data.locations.replaceLastSublist(currentLocationSequence).map { it.toImmutableList() }.toImmutableList(),
                        distanceMeters = data.locations.distanceSequenceMeters,
                        speed = location.location.speed ?: currentLocationSequence.currentSpeed,
                        heartRates = heartRates.toImmutableList()
                    )
                }
            }.launchIn(applicationScope)

        duration.onEach {
            watchConnector.sendMessageToWatch(MessagingAction.DurationUpdate(it))
        }.launchIn(applicationScope)

        activityData
            .map { it.distanceMeters }
            .distinctUntilChanged()
            .onEach {
                watchConnector.sendMessageToWatch(MessagingAction.DistanceUpdate(it))
            }.launchIn(applicationScope)
    }

    fun setIsTrackingActivity(active: Boolean) {
        isTrackingActivity.value = active
    }

    fun setActivityStatus(status: ActivityStatus) {
        _activityStatus.value = status
    }

    fun startTrackingLocation() {
        isTrackingLocation.value = true
        watchConnector.setCanTrack(true)
    }

    fun stopTrackingLocation() {
        isTrackingLocation.value = false
        watchConnector.setCanTrack(false)
    }

    fun clear() {
        stopTrackingLocation()
        setIsTrackingActivity(false)
        setActivityStatus(ActivityStatus.NOT_STARTED)

        _duration.value = Duration.ZERO
        _activityData.value = ActivityData()
    }
}
