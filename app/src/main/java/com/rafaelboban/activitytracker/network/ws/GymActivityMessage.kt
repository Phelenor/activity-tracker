package com.rafaelboban.activitytracker.network.ws

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class GymActivityMessage {

    @Serializable
    @SerialName("control_action")
    data class ControlAction(
        val action: ActivityControlAction
    ) : GymActivityMessage()

    @Serializable
    @SerialName("data_snapshot")
    data class DataSnapshot(
        val distance: Int,
        val heartRate: Int,
        val speed: Float,
        val avgSpeed: Float,
        val avgHeartRate: Int,
        val elevationGain: Int,
        val calories: Int
    ) : GymActivityMessage()
}
