package com.rafaelboban.activitytracker.network.ws

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class ActivityMessage {

    @Serializable
    @SerialName("connect_message")
    data class ConnectMessage(val data: String) : ActivityMessage()

    @Serializable
    @SerialName("data_update")
    data class DataUpdate(val data: String) : ActivityMessage()

    @Serializable
    @SerialName("status_change")
    data class StatusChange(val data: String) : ActivityMessage()
}
