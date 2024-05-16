package com.rafaelboban.activitytracker.network.repository

import com.rafaelboban.activitytracker.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(private val api: ApiService) {

    suspend fun getWeatherData(latitude: Float, longitude: Float, hours: Int = 4) = api.getWeatherData(latitude, longitude, hours)
}
