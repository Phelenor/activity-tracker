package com.rafaelboban.activitytracker.network.model

import kotlinx.serialization.Serializable

@Serializable
data class TokenRefreshRequest(
    val refreshToken: String
)
