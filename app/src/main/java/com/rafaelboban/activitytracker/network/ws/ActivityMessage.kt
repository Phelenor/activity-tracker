package com.rafaelboban.activitytracker.network.ws

import com.rafaelboban.activitytracker.model.network.GroupActivity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration

enum class ActivityControlAction {
    START, PAUSE, RESUME, FINISH
}

@Serializable
sealed class ActivityMessage {

    @Serializable
    @SerialName("activity_update")
    data class GroupActivityUpdate(
        val activity: GroupActivity
    ) : ActivityMessage()

    @Serializable
    @SerialName("user_finish")
    data class UserFinish(
        val userId: String,
        val durationSeconds: Int
    ) : ActivityMessage()

    @Serializable
    @SerialName("user_update")
    data class UserDataSnapshot(
        val userId: String,
        val userDisplayName: String,
        val userImageUrl: String?,
        val lat: Float,
        val long: Float,
        val distance: Int,
        val heartRate: Int,
        val speed: Float,
        val duration: Duration? = null
    ) : ActivityMessage()

    @Serializable
    @SerialName("control_action")
    data class ControlAction(
        val action: ActivityControlAction
    ) : ActivityMessage()
}
