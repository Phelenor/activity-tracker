package com.rafaelboban.activitytracker.model

import com.rafaelboban.activitytracker.util.DateHelper
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
    val birthTimestamp: Long?,
    @SerialName("display_name")
    val displayName: String
) {

    val age: Int?
        get() = birthTimestamp?.let { DateHelper.getYearsSince(it) }
}
