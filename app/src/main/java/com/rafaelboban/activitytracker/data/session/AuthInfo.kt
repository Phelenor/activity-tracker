package com.rafaelboban.activitytracker.data.session

import com.rafaelboban.activitytracker.model.User
import kotlinx.serialization.Serializable

@Serializable
data class AuthInfo(
    val user: User,
    val accessToken: String,
    val refreshToken: String
)
