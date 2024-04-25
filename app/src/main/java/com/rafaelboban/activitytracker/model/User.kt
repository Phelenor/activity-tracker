package com.rafaelboban.activitytracker.model

import com.squareup.moshi.Json

data class User(
    val id: String,
    val email: String,
    val imageUrl: String,
    val name: String,
    @Json(name = "display_name")
    val displayName: String
)
