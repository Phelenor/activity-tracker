package com.rafaelboban.activitytracker.network.repository

import com.rafaelboban.activitytracker.network.ApiService
import com.rafaelboban.activitytracker.network.model.LoginRequest
import com.rafaelboban.activitytracker.network.model.TokenRefreshRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(private val api: ApiService) {

    suspend fun login(body: LoginRequest) = api.login(body)

    suspend fun refreshToken(body: TokenRefreshRequest) = api.refreshToken(body)

    suspend fun ping() = api.ping()
}
