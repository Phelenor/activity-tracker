package com.rafaelboban.activitytracker.network

import com.rafaelboban.activitytracker.BuildConfig
import com.rafaelboban.activitytracker.model.User
import com.rafaelboban.activitytracker.network.model.LoginRequest
import com.rafaelboban.activitytracker.network.model.LoginResponse
import com.rafaelboban.activitytracker.network.model.UpdateUserData
import com.rafaelboban.activitytracker.network.model.weather.WeatherForecast
import com.skydoves.sandwich.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("/")
    suspend fun ping(): ApiResponse<Unit>

    @POST("/api/login")
    suspend fun login(@Body body: LoginRequest): ApiResponse<LoginResponse>

    @POST("/api/delete-account")
    suspend fun deleteAccount(): ApiResponse<Unit>

    @POST("/api/update-user")
    suspend fun updateUserData(@Body body: UpdateUserData): ApiResponse<User>

    @GET("https://api.openweathermap.org/data/3.0/onecall")
    suspend fun getWeatherData(
        @Query("lat") latitude: Float,
        @Query("lon") longitude: Float,
        @Query("cnt") hours: Int,
        @Query("units") units: String = "metric",
        @Query("exclude") exclude: String = "daily,minutely",
        @Query("appid") apiKey: String = BuildConfig.OPEN_WEATHER_KEY
    ): ApiResponse<WeatherForecast>
}
