package com.rafaelboban.activitytracker.network.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val idToken: String,
    val nonce: String
)
