package com.rafaelboban.activitytracker.model.network

import com.rafaelboban.core.shared.model.ActivityStatus
import com.rafaelboban.core.shared.model.ActivityType
import kotlinx.serialization.Serializable

@Serializable
data class GroupActivity(
    val id: String,
    val joinCode: String,
    val userOwnerId: String,
    val startTimestamp: Long?,
    val activityType: ActivityType,
    val status: ActivityStatus,
    val startedUsers: List<String>,
    val activeUsers: List<String>
)
