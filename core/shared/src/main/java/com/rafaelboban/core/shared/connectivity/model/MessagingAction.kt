package com.rafaelboban.core.shared.connectivity.model

import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.shared.model.HeartRatePoint
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
sealed interface MessagingAction {

    @Serializable
    data object Start : MessagingAction

    @Serializable
    data object Resume : MessagingAction

    @Serializable
    data object Pause : MessagingAction

    @Serializable
    data object Finish : MessagingAction

    @Serializable
    data object CanTrack : MessagingAction

    @Serializable
    data object CanNotTrack : MessagingAction

    @Serializable
    data class SetActivityData(val activityType: ActivityType?, val userAge: Int?) : MessagingAction

    @Serializable
    data object ConnectionRequest : MessagingAction

    @Serializable
    data class HeartRateUpdate(val heartRatePoint: HeartRatePoint) : MessagingAction

    @Serializable
    data class CaloriesUpdate(val calories: Int) : MessagingAction

    @Serializable
    data class DistanceUpdate(val distanceMeters: Int) : MessagingAction

    @Serializable
    data class SpeedUpdate(val speed: Float) : MessagingAction

    @Serializable
    data class DurationUpdate(val duration: Duration) : MessagingAction

    @Serializable
    data object OpenAppOnPhone : MessagingAction

    @Serializable
    data object WakeUpWatch : MessagingAction

    @Serializable
    data class GroupActivityMarker(val isActivityOwner: Boolean) : MessagingAction
}
