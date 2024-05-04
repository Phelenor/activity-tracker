package com.rafaelboban.activitytracker.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val imageUrl: String?,
    val name: String,
    val weight: Int?,
    val height: Int?,
    @SerialName("display_name")
    val displayName: String
)
