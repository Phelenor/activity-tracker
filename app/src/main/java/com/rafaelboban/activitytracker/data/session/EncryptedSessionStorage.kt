package com.rafaelboban.activitytracker.data.session

import android.content.SharedPreferences
import com.rafaelboban.activitytracker.di.PreferencesEncrypted
import com.rafaelboban.activitytracker.util.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class EncryptedSessionStorage @Inject constructor(
    @PreferencesEncrypted private val preferences: SharedPreferences
) {

    suspend fun get(): AuthInfo? {
        return withContext(Dispatchers.IO) {
            val json = preferences.getString(AUTH_INFO, null)

            json?.let {
                Json.decodeFromString<AuthInfo>(json)
            }
        }
    }

    suspend fun set(info: AuthInfo?) {
        withContext(Dispatchers.IO) {
            if (info == null) {
                preferences.edit { remove(AUTH_INFO) }
                return@withContext
            }

            val json = Json.encodeToString(info)
            preferences.edit { putString(AUTH_INFO, json) }
        }
    }

    suspend fun clear() {
        set(null)
    }

    companion object {
        private const val AUTH_INFO = "AUTH_INFO"
    }
}
