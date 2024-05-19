@file:OptIn(ExperimentalCoroutinesApi::class)

package com.rafaelboban.activitytracker.tracking

import com.rafaelboban.activitytracker.model.ActivityData
import com.rafaelboban.activitytracker.model.location.LocationTimestamp
import com.rafaelboban.activitytracker.network.model.goals.ActivityGoal
import com.rafaelboban.activitytracker.network.model.goals.ActivityGoalProgress
import com.rafaelboban.activitytracker.network.model.goals.ActivityGoalType
import com.rafaelboban.activitytracker.network.model.goals.GoalValueComparisonType
import com.rafaelboban.activitytracker.util.UserData
import com.rafaelboban.activitytracker.util.currentSpeed
import com.rafaelboban.activitytracker.util.distanceSequenceMeters
import com.rafaelboban.activitytracker.util.elevationGain
import com.rafaelboban.core.shared.connectivity.connectors.PhoneToWatchConnector
import com.rafaelboban.core.shared.connectivity.model.MessagingAction
import com.rafaelboban.core.shared.model.ActivityStatus
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.shared.model.HeartRatePoint
import com.rafaelboban.core.shared.utils.DEFAULT_HEART_RATE_TRACKER_AGE
import com.rafaelboban.core.shared.utils.F
import com.rafaelboban.core.shared.utils.HeartRateZone
import com.rafaelboban.core.shared.utils.HeartRateZoneHelper
import com.rafaelboban.core.shared.utils.replaceLastSublist
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class ActivityTracker(
    private val applicationScope: CoroutineScope,
    private val locationObserver: LocationObserver,
    private val watchConnector: PhoneToWatchConnector
) {

    private val _type = MutableStateFlow<ActivityType?>(null)
    val type = _type.asStateFlow()

    private val _data = MutableStateFlow(ActivityData())
    val data = _data.asStateFlow()

    private val _duration = MutableStateFlow(Duration.ZERO)
    val duration = _duration.asStateFlow()

    private val _status = MutableStateFlow(ActivityStatus.NOT_STARTED)
    val status = _status.asStateFlow()

    private val _goals = MutableStateFlow(emptyList<ActivityGoalProgress>())
    val goals = _goals.asStateFlow()

    private val isTrackingActivity = MutableStateFlow(false)
    private val isTrackingLocation = MutableStateFlow(false)

    private var goalProgressUpdateJob: Job? = null

    val currentLocation = isTrackingLocation
        .flatMapLatest { isObservingLocation ->
            if (isObservingLocation) {
                locationObserver.observeLocation((1.5).seconds.inWholeMilliseconds)
            } else {
                flowOf()
            }
        }.onEach { location ->
            if (location.speed != null && !isTrackingActivity.value) {
                _data.update { data ->
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

    private val calories = watchConnector.messages
        .filterIsInstance<MessagingAction.CaloriesUpdate>()
        .map { it.calories }
        .stateIn(
            applicationScope,
            SharingStarted.Lazily,
            null
        )

    init {
        isTrackingActivity.onEach { isTracking ->
            if (isTracking.not()) {
                _data.update { data ->
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
                _data.update { data ->
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

        calories.filterNotNull()
            .onEach { calories ->
                _data.update { data ->
                    data.copy(caloriesBurned = calories)
                }
            }.launchIn(applicationScope)

        currentLocation
            .filterNotNull()
            .combineTransform(isTrackingActivity) { location, isTracking ->
                if (isTracking) {
                    emit(location)
                } else {
                    if (location.speed == null) {
                        _data.update { data ->
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
                _data.update { data ->
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
                        currentHeartRate = data.currentHeartRate,
                        caloriesBurned = data.caloriesBurned
                    )
                }
            }.launchIn(applicationScope)

        duration.onEach { duration ->
            watchConnector.sendMessageToWatch(MessagingAction.DurationUpdate(duration))
        }.launchIn(applicationScope)

        data.map { it.distanceMeters }
            .distinctUntilChanged()
            .onEach { distance ->
                watchConnector.sendMessageToWatch(MessagingAction.DistanceUpdate(distance))
            }.launchIn(applicationScope)

        data.map { it.speed }
            .distinctUntilChanged()
            .onEach { speed ->
                watchConnector.sendMessageToWatch(MessagingAction.SpeedUpdate(speed))
            }.launchIn(applicationScope)

        _type.onEach { type ->
            watchConnector.sendMessageToWatch(MessagingAction.SetActivityData(type, UserData.user?.age))
        }.launchIn(applicationScope)
    }

    fun setIsTrackingActivity(active: Boolean) {
        isTrackingActivity.value = active
    }

    fun setActivityStatus(status: ActivityStatus) {
        _status.value = status

        when (status) {
            ActivityStatus.IN_PROGRESS -> {
                val wasNotStarted = _data.value.startTimestamp == null
                if (wasNotStarted) {
                    _data.update { data ->
                        data.copy(startTimestamp = Instant.now().epochSecond)
                    }
                }
            }

            ActivityStatus.FINISHED -> {
                _data.update { data ->
                    data.copy(endTimestamp = Instant.now().epochSecond)
                }
            }

            else -> Unit
        }
    }

    fun addGoal(goal: ActivityGoal) {
        _goals.update { currentGoals ->
            currentGoals + ActivityGoalProgress(
                goal = goal,
                currentValue = 0f
            )
        }

        startGoalProgressUpdates()
    }

    fun removeGoal(goalType: ActivityGoalType) {
        _goals.update { currentGoals ->
            currentGoals.filterNot { it.goal.type == goalType }
        }

        if (_goals.value.isEmpty()) {
            goalProgressUpdateJob?.cancel()
        }
    }

    fun startTrackingLocation(type: ActivityType) {
        _type.value = type
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

        _type.value = null
        _duration.value = Duration.ZERO
        _data.value = ActivityData()
    }

    private fun startGoalProgressUpdates() {
        goalProgressUpdateJob?.cancel()
        goalProgressUpdateJob = applicationScope.launch {
            while (isActive) {
                val distribution = HeartRateZoneHelper.calculateHeartRateZoneDistribution(heartRates.value, UserData.user?.age ?: DEFAULT_HEART_RATE_TRACKER_AGE, _duration.value)

                _goals.update {
                    goals.value.map { it.goal }.map { goal ->
                        ActivityGoalProgress(
                            goal = goal,
                            currentValue = when (goal.type) {
                                ActivityGoalType.DISTANCE -> _data.value.distanceMeters / 1000f
                                ActivityGoalType.DURATION -> _duration.value.inWholeSeconds.F
                                ActivityGoalType.CALORIES -> calories.value?.F ?: 0f
                                ActivityGoalType.AVG_HEART_RATE -> if (heartRates.value.isEmpty()) 0f else heartRates.value.map { it.heartRate }.average().F
                                ActivityGoalType.AVG_SPEED -> if (_duration.value == Duration.ZERO) 0f else (_data.value.distanceMeters / 1000f) / (_duration.value.inWholeSeconds / 3600f)
                                ActivityGoalType.AVG_PACE -> if (_duration.value == Duration.ZERO) 0f else 60f / (_data.value.distanceMeters / 1000f) / (_duration.value.inWholeSeconds / 3600f)
                                ActivityGoalType.IN_HR_ZONE -> {
                                    goal.label?.toInt()?.let { index ->
                                        val zone = HeartRateZone.Trackable[index]
                                        distribution?.get(zone) ?: 0f
                                    } ?: run {
                                        0f
                                    }
                                }

                                ActivityGoalType.BELOW_ABOVE_HR_ZONE -> {
                                    goal.label?.toInt()?.let { index ->
                                        val zones = if (goal.valueType == GoalValueComparisonType.GREATER) {
                                            HeartRateZone.entries.subList(index, HeartRateZone.Trackable.size)
                                        } else {
                                            HeartRateZone.entries.subList(0, index + 1)
                                        }

                                        zones.map { zone -> distribution?.get(zone) ?: 0f }.sum()
                                    } ?: run {
                                        0f
                                    }
                                }
                            }
                        )
                    }
                }

                delay(5.seconds)
            }
        }
    }
}
