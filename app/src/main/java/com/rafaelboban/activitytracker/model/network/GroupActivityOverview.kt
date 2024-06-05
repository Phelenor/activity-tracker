package com.rafaelboban.activitytracker.model.network

import com.rafaelboban.activitytracker.model.User
import kotlinx.serialization.Serializable

@Serializable
data class GroupActivityOverview(
    val users: List<User>,
    val ownerId: String
)
