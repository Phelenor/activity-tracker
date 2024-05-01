package com.rafaelboban.activitytracker.network.model

import com.rafaelboban.activitytracker.model.User

data class LoginResponse(
    val user: User,
    val accessToken: String,
    val refreshToken: String
)
