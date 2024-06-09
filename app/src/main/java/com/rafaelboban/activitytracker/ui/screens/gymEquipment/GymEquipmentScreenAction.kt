package com.rafaelboban.activitytracker.ui.screens.gymEquipment

sealed interface GymEquipmentScreenAction {
    data object OnBackClick : GymEquipmentScreenAction
    data object OnRetryClick : GymEquipmentScreenAction
}
