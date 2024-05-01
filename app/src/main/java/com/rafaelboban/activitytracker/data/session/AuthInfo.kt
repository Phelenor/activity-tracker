package com.rafaelboban.activitytracker.data.session

import com.rafaelboban.activitytracker.model.User

data class AuthInfo(
    val user: User,
    val accessToken: String,
    val refreshToken: String
)
