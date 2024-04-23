package com.rafaelboban.activitytracker.network

import com.rafaelboban.activitytracker.network.model.LoginRequest
import com.rafaelboban.activitytracker.network.model.LoginResponse
import com.skydoves.sandwich.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET("/")
    suspend fun test(): ApiResponse<Unit>

    @POST("/login")
    suspend fun login(@Body body: LoginRequest): ApiResponse<LoginResponse>

}
