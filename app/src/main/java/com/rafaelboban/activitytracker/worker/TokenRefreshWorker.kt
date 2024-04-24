package com.rafaelboban.activitytracker.worker

import android.content.Context
import android.content.SharedPreferences
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.rafaelboban.activitytracker.network.model.TokenRefreshRequest
import com.rafaelboban.activitytracker.network.repository.UserRepository
import com.rafaelboban.activitytracker.util.Constants.AUTH_TOKEN
import com.rafaelboban.activitytracker.util.Constants.USER_ID
import com.rafaelboban.activitytracker.util.UserData
import com.skydoves.sandwich.getOrNull
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class TokenRefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val userRepository: UserRepository,
    private val preferences: SharedPreferences,
) : CoroutineWorker(appContext, workerParams) {

    companion object {

        fun enqueue(context: Context) = buildOneTimeWorkRequest<TokenRefreshWorker>(context)
    }

    override suspend fun doWork(): Result {
        val userId = preferences.getString(USER_ID, null) ?: return Result.failure()
        val response = userRepository.refreshToken(TokenRefreshRequest(userId))

        response.getOrNull()?.let { data ->
            preferences.edit().putString(AUTH_TOKEN, data.token).apply()
            UserData.user = data.user
            return Result.success()
        } ?: run {
            return Result.failure()
        }
    }
}
