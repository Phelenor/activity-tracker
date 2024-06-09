package com.rafaelboban.activitytracker.network.repository

import com.rafaelboban.activitytracker.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GymRepository @Inject constructor(
    private val api: ApiService
) {

    suspend fun checkIfEquipmentExists(id: String) = api.checkIfEquipmentExists(id)

    suspend fun getEquipment(id: String) = api.getEquipment(id)
}
