@file:OptIn(ExperimentalCoroutinesApi::class)

package com.rafaelboban.activitytracker.tracking

import com.rafaelboban.activitytracker.model.ActivityData
import com.rafaelboban.activitytracker.model.location.LocationTimestamp
import com.rafaelboban.activitytracker.util.currentSpeed
import com.rafaelboban.activitytracker.util.distanceSequenceMeters
import com.rafaelboban.activitytracker.util.replaceLastSublist
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

    private val isTrackingLocation = MutableStateFlow(false)

    val currentLocation = isTrackingLocation
        .flatMapLatest { isObservingLocation ->
            if (isObservingLocation) {
                locationObserver.observeLocation(2000L)
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
