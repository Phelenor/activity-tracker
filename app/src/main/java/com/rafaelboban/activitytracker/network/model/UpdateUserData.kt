package com.rafaelboban.activitytracker.network.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserData(
    val name: String?,
    val height: Int?,
    val weight: Int?,
    val birthTimestamp: Long?
)
