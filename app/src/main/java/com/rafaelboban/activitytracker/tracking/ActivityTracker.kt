@file:OptIn(ExperimentalCoroutinesApi::class)

package com.rafaelboban.activitytracker.tracking

import com.rafaelboban.activitytracker.model.ActivityData
import com.rafaelboban.activitytracker.model.location.LocationTimestamp
import com.rafaelboban.activitytracker.util.UserData
import com.rafaelboban.activitytracker.util.currentSpeed
import com.rafaelboban.activitytracker.util.distanceSequenceMeters
import com.rafaelboban.activitytracker.util.elevationGain
import com.rafaelboban.core.shared.connectivity.connectors.PhoneToWatchConnector
import com.rafaelboban.core.shared.connectivity.model.MessagingAction
import com.rafaelboban.core.shared.model.ActivityStatus
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.shared.model.HeartRatePoint
import com.rafaelboban.core.shared.utils.replaceLastSublist
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
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

    private val _activityType = MutableStateFlow<ActivityType?>(null)
    val activityType = _activityType.asStateFlow()

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
        }.onEach { location ->
            if (location.speed != null && !isTrackingActivity.value) {
                _activityData.update { data ->
                    data.copy(
                        speed = location.speed
                    )
                }
            }
        }.stateIn(
            applicationScope,
            SharingStarted.Lazily,
            null
        )

    private val heartRates = watchConnector.messages
        .filterIsInstance<MessagingAction.HeartRateUpdate>()
        .map { it.heartRatePoint }
        .runningFold(initial = emptyList<HeartRatePoint>()) { currentHeartRates, newHeartRate ->
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

        heartRates.onEach { heartRates ->
            if (heartRates.isNotEmpty()) {
                _activityData.update { data ->
                    data.copy(
                        currentHeartRate = heartRates.last(),
                        heartRatePoints = if (isTrackingActivity.value) {
                            (data.heartRatePoints + heartRates.last()).toImmutableList()
                        } else {
                            data.heartRatePoints
                        }
                    )
                }
            }
        }.launchIn(applicationScope)

        currentLocation
            .filterNotNull()
            .combineTransform(isTrackingActivity) { location, isTracking ->
                if (isTracking) {
                    emit(location)
                } else {
                    if (location.speed == null) {
                        _activityData.update { data ->
                            data.copy(speed = 0f)
                        }
                    }
                }
            }.zip(_duration) { location, duration ->
                LocationTimestamp(
                    location = location,
                    durationTimestamp = duration
                )
            }.map { location ->
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
                        elevationGain = data.locations.elevationGain,
                        speed = location.location.speed ?: currentLocationSequence.currentSpeed,
                        heartRatePoints = data.heartRatePoints,
                        currentHeartRate = data.currentHeartRate
                    )
                }
            }.launchIn(applicationScope)

        duration.onEach { duration ->
            watchConnector.sendMessageToWatch(MessagingAction.DurationUpdate(duration))
        }.launchIn(applicationScope)

        activityData
            .map { it.distanceMeters }
            .distinctUntilChanged()
            .onEach { distance ->
                watchConnector.sendMessageToWatch(MessagingAction.DistanceUpdate(distance))
            }.launchIn(applicationScope)

        activityData
            .map { it.speed }
            .distinctUntilChanged()
            .onEach { speed ->
                watchConnector.sendMessageToWatch(MessagingAction.SpeedUpdate(speed))
            }.launchIn(applicationScope)

        _activityType
            .onEach { type ->
                watchConnector.sendMessageToWatch(MessagingAction.SetActivityData(type, UserData.user?.age))
            }.launchIn(applicationScope)
    }

    fun setIsTrackingActivity(active: Boolean) {
        isTrackingActivity.value = active
    }

    fun setActivityStatus(status: ActivityStatus) {
        _activityStatus.value = status
    }

    fun startTrackingLocation(type: ActivityType) {
        _activityType.value = type
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

        _activityType.value = null
        _duration.value = Duration.ZERO
        _activityData.value = ActivityData()
    }
}
