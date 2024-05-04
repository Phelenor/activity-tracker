package com.rafaelboban.activitytracker.network.model

import com.rafaelboban.activitytracker.model.User
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val user: User,
    val accessToken: String,
    val refreshToken: String
)
