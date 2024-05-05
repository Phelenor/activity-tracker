@file:OptIn(ExperimentalCoroutinesApi::class)

package com.rafaelboban.activitytracker.tracking

import com.rafaelboban.activitytracker.model.ActivityData
import com.rafaelboban.activitytracker.model.location.LocationTimestamp
import com.rafaelboban.activitytracker.util.currentSpeed
import com.rafaelboban.activitytracker.util.distanceMeters
import com.rafaelboban.activitytracker.util.distanceSequenceMeters
import com.rafaelboban.activitytracker.util.replaceLastSublist
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

    private val isActive = MutableStateFlow(false)

    private val isTrackingLocation = MutableStateFlow(false)

    val currentLocation = isTrackingLocation
        .flatMapLatest { isObservingLocation ->
            if (isObservingLocation) {
                locationObserver.observeLocation(2000L)
            } else flowOf()
        }
        .stateIn(
            applicationScope,
            SharingStarted.Lazily,
            null
        )

    init {
        isActive.flatMapLatest { isActive ->
            if (isActive) Timer.time() else flowOf()
        }.onEach { interval ->
            _duration.update { it + interval }
        }.launchIn(applicationScope)

        currentLocation
            .filterNotNull()
            .combineTransform(isActive) { location, isActive ->
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
                    val currentLocationSequence = if (data.locations.isNotEmpty()) {
                        data.locations.last() + location
                    } else {
                        listOf(location)
                    }

                    ActivityData(
                        locations = data.locations.replaceLastSublist(currentLocationSequence),
                        distanceMeters = data.locations.distanceSequenceMeters,
                        speed = currentLocationSequence.currentSpeed
                    )
                }
            }
    }

    fun setIsActive(active: Boolean) {
        isActive.value = active
    }

    fun startTrackingLocation() {
        isTrackingLocation.value = true
    }

    fun stopTrackingLocation() {
        isTrackingLocation.value = false
    }
}
