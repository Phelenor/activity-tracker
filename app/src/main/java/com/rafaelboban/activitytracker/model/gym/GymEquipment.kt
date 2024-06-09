package com.rafaelboban.activitytracker.model.gym

import com.rafaelboban.core.shared.model.ActivityType
import kotlinx.serialization.Serializable

@Serializable
data class GymEquipment(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val videoUrl: String?,
    val activityType: ActivityType
)
