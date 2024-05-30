package com.rafaelboban.activitytracker.network

import com.rafaelboban.activitytracker.BuildConfig
import com.rafaelboban.activitytracker.model.User
import com.rafaelboban.activitytracker.model.network.Activity
import com.rafaelboban.activitytracker.model.network.CreateGroupActivityRequest
import com.rafaelboban.activitytracker.model.network.GroupActivity
import com.rafaelboban.activitytracker.model.network.JoinGroupActivityRequest
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

    @GET("/api/activities/{id}")
    suspend fun getActivity(@Path("id") id: String): ApiResponse<Activity>

    @DELETE("/api/activities/{id}")
    suspend fun deleteActivity(@Path("id") id: String): ApiResponse<String?>

    @POST("/api/create-group-activity")
    suspend fun createGroupActivity(@Body body: CreateGroupActivityRequest): ApiResponse<GroupActivity>

    @POST("/api/join-group-activity")
    suspend fun joinGroupActivity(@Body body: JoinGroupActivityRequest): ApiResponse<GroupActivity>

    @GET("/api/group-activities/{id}")
    suspend fun getGroupActivity(@Path("id") id: String): ApiResponse<GroupActivity>

    @GET("/api/group-activities")
    suspend fun getGroupActivities(): ApiResponse<List<GroupActivity>>

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
