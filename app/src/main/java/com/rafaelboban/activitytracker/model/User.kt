package com.rafaelboban.activitytracker.model

import com.squareup.moshi.Json

data class User(
    val id: String,
    val email: String,
    val imageUrl: String?,
    val name: String,
    val weight: Int?,
    val height: Int?,
    @Json(name = "display_name")
    val displayName: String
)
