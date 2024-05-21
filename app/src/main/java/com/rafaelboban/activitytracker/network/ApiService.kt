package com.rafaelboban.activitytracker.network

import com.rafaelboban.activitytracker.BuildConfig
import com.rafaelboban.activitytracker.model.User
import com.rafaelboban.activitytracker.model.network.Activity
import com.rafaelboban.activitytracker.network.model.LoginRequest
import com.rafaelboban.activitytracker.network.model.LoginResponse
import com.rafaelboban.activitytracker.network.model.UpdateUserData
import com.rafaelboban.activitytracker.network.model.weather.WeatherForecast
import com.skydoves.sandwich.ApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("/api/login")
    suspend fun login(@Body body: LoginRequest): ApiResponse<LoginResponse>

    @POST("/api/delete-account")
    suspend fun deleteAccount(): ApiResponse<Unit>

    @POST("/api/update-user")
    suspend fun updateUserData(@Body body: UpdateUserData): ApiResponse<User>

    @Multipart
    @POST("/api/activities")
    suspend fun postActivity(@Part("activity") activity: RequestBody, @Part image: MultipartBody.Part): ApiResponse<Activity>

    @GET("/api/activities")
    suspend fun getActivities(): ApiResponse<List<Activity>>

    @DELETE("/api/activities/{id}")
    suspend fun deleteActivity(@Path("id") id: String): ApiResponse<String?>

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
