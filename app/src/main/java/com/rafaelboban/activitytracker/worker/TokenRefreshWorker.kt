package com.rafaelboban.activitytracker.worker

import android.content.Context
import android.content.SharedPreferences
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rafaelboban.activitytracker.di.PreferencesEncrypted
import com.rafaelboban.activitytracker.network.model.TokenRefreshRequest
import com.rafaelboban.activitytracker.network.repository.UserRepository
import com.rafaelboban.activitytracker.util.Constants.AUTH_TOKEN
import com.rafaelboban.activitytracker.util.Constants.USER_DATA
import com.rafaelboban.activitytracker.util.Constants.USER_ID
import com.rafaelboban.activitytracker.util.UserData
import com.rafaelboban.activitytracker.util.edit
import com.rafaelboban.activitytracker.util.objectToJson
import com.skydoves.sandwich.getOrNull
import com.squareup.moshi.Moshi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TokenRefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val moshi: Moshi,
    private val userRepository: UserRepository,
    @PreferencesEncrypted private val preferences: SharedPreferences
) : CoroutineWorker(appContext, workerParams) {

    companion object {

        fun enqueue(context: Context) = buildOneTimeWorkRequest<TokenRefreshWorker>(context)
    }

    override suspend fun doWork(): Result {
        val userId = preferences.getString(USER_ID, null) ?: return Result.failure()
        val response = userRepository.refreshToken(TokenRefreshRequest(userId))

        response.getOrNull()?.let { data ->
            UserData.user = data.user
            preferences.edit {
                putString(AUTH_TOKEN, data.token)
                putString(USER_DATA, moshi.objectToJson(data.user))
            }
            return Result.success()
        } ?: run {
            return Result.failure()
        }
    }
}
