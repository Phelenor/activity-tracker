package com.rafaelboban.activitytracker.network.model

data class LoginRequest(
    val idToken: String,
    val nonce: String
)
