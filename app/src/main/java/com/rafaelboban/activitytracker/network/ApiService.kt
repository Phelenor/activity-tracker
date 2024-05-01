package com.rafaelboban.activitytracker.network

import com.rafaelboban.activitytracker.model.User
import com.rafaelboban.activitytracker.network.model.ChangeNameRequest
import com.rafaelboban.activitytracker.network.model.LoginRequest
import com.rafaelboban.activitytracker.network.model.LoginResponse
import com.skydoves.sandwich.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET("/")
    suspend fun ping(): ApiResponse<Unit>

    @POST("/api/login")
    suspend fun login(@Body body: LoginRequest): ApiResponse<LoginResponse>

    @POST("/api/delete-account")
    suspend fun deleteAccount(): ApiResponse<Unit>

    @POST("/api/change-name")
    suspend fun changeUserName(@Body body: ChangeNameRequest): ApiResponse<User>
}
