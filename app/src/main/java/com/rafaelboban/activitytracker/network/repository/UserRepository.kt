package com.rafaelboban.activitytracker.network.repository

import com.rafaelboban.activitytracker.network.ApiService
import com.rafaelboban.activitytracker.network.model.LoginRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(private val api: ApiService) {

    suspend fun login(body: LoginRequest) = api.login(body)

    suspend fun test() = api.test()
}
