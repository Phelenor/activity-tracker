package com.rafaelboban.core.shared.connectivity.model

import com.rafaelboban.core.shared.model.ActivityType
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
    data class SetActivityType(val activityType: ActivityType?) : MessagingAction

    @Serializable
    data object ConnectionRequest : MessagingAction

    @Serializable
    data class HeartRateUpdate(val heartRate: Int) : MessagingAction

    @Serializable
    data class CaloriesUpdate(val calories: Int) : MessagingAction

    @Serializable
    data class DistanceUpdate(val distanceMeters: Int) : MessagingAction

    @Serializable
    data class DurationUpdate(val duration: Duration) : MessagingAction

    @Serializable
    data object OpenAppOnPhone : MessagingAction

    @Serializable
    data object WakeUpWatch : MessagingAction
}
