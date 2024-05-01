package com.rafaelboban.activitytracker.data.session

import android.content.SharedPreferences
import com.rafaelboban.activitytracker.di.PreferencesEncrypted
import com.rafaelboban.activitytracker.util.edit
import com.rafaelboban.activitytracker.util.jsonToObject
import com.rafaelboban.activitytracker.util.objectToJson
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EncryptedSessionStorage @Inject constructor(
    private val moshi: Moshi,
    @PreferencesEncrypted private val preferences: SharedPreferences
) {

    suspend fun get(): AuthInfo? {
        return withContext(Dispatchers.IO) {
            val json = preferences.getString(AUTH_INFO, null)

            json?.let {
                moshi.jsonToObject<AuthInfo>(json)
            }
        }
    }

    suspend fun set(info: AuthInfo?) {
        withContext(Dispatchers.IO) {
            if (info == null) {
                preferences.edit { remove(AUTH_INFO) }
                return@withContext
            }

            val json = moshi.objectToJson(info)
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
