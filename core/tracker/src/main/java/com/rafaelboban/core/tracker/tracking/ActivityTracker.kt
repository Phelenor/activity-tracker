@file:OptIn(ExperimentalCoroutinesApi::class)

package com.rafaelboban.core.tracker.tracking

import com.rafaelboban.core.tracker.model.ActivityData
import com.rafaelboban.core.tracker.model.ActivityStatus
import com.rafaelboban.core.tracker.model.location.LocationTimestamp
import com.rafaelboban.core.tracker.utils.currentSpeed
import com.rafaelboban.core.tracker.utils.distanceSequenceMeters
import com.rafaelboban.core.tracker.utils.replaceLastSublist
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.zip
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class ActivityTracker(
    applicationScope: CoroutineScope,
    private val locationObserver: LocationObserver
) {

    private val _activityData = MutableStateFlow(ActivityData())
    val activityData = _activityData.asStateFlow()

    private val _duration = MutableStateFlow(Duration.ZERO)
    val duration = _duration.asStateFlow()

    private val _isActive = MutableStateFlow(false)
    val isActive = _isActive.asStateFlow()

    private val _activityStatus = MutableStateFlow(ActivityStatus.NOT_STARTED)
    val activityStatus = _activityStatus.asStateFlow()

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

    init {
        _isActive.onEach { isActive ->
            if (!isActive) {
                _activityData.update { data ->
                    data.copy(locations = (data.locations + listOf(persistentListOf())).toImmutableList())
                }
            }
        }.flatMapLatest { isActive ->
            if (isActive) Timer.time() else flowOf()
        }.onEach { interval ->
            _duration.update { it + interval }
        }.launchIn(applicationScope)

        currentLocation
            .filterNotNull()
            .combineTransform(_isActive) { location, isActive ->
                if (isActive) {
                    emit(location)
                }
            }.zip(_duration) { location, duration ->
                LocationTimestamp(
                    location = location,
                    durationTimestamp = duration
                )
            }.onEach { location ->
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
                        speed = location.location.speed ?: currentLocationSequence.currentSpeed
                    )
                }
            }.launchIn(applicationScope)
    }

    fun setIsActive(active: Boolean) {
        _isActive.value = active
    }

    fun setStatus(status: ActivityStatus) {
        _activityStatus.value = status
    }

    fun startTrackingLocation() {
        isTrackingLocation.value = true
    }

    private fun stopTrackingLocation() {
        isTrackingLocation.value = false
    }

    fun stop() {
        stopTrackingLocation()
        setIsActive(false)
    }

    fun clear() {
        stop()

        _duration.value = Duration.ZERO
        _activityData.value = ActivityData()
    }
}
