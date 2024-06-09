package com.rafaelboban.activitytracker.ui.screens.gymEquipment

import com.rafaelboban.activitytracker.model.gym.GymEquipment

data class GymEquipmentScreenState(
    val equipment: GymEquipment? = null,
    val isLoading: Boolean = true
)
