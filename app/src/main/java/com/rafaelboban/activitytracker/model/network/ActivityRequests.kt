package com.rafaelboban.activitytracker.model.network

import com.rafaelboban.core.shared.model.ActivityType
import kotlinx.serialization.Serializable

@Serializable
data class CreateGroupActivityRequest(
    val activityType: ActivityType,
    val startTimestamp: Long?
)

@Serializable
data class JoinGroupActivityRequest(
    val joinCode: String
)
