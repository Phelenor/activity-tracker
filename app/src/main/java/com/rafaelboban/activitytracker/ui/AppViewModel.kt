package com.rafaelboban.activitytracker.ui

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.auth0.android.jwt.JWT
import com.rafaelboban.activitytracker.di.PreferencesEncrypted
import com.rafaelboban.activitytracker.util.Constants.AUTH_TOKEN
import com.rafaelboban.activitytracker.util.Constants.USER_DATA
import com.rafaelboban.activitytracker.util.UserData
import com.rafaelboban.activitytracker.util.edit
import com.rafaelboban.activitytracker.util.get
import com.rafaelboban.activitytracker.util.jsonToObject
import com.squareup.moshi.Moshi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    application: Application,
    private val moshi: Moshi,
    @PreferencesEncrypted private val preferences: SharedPreferences
) : AndroidViewModel(application) {

    private val _showSplashScreen = MutableStateFlow(true)
    val showSplashScreen = _showSplashScreen.asStateFlow()

    var isAuthTokenValid = false
        private set

    fun initAndSplashDelay() {
        viewModelScope.launch {
            val splashDelay = async { delay(200) }

            isAuthTokenValid = checkAuthToken()
            if (isAuthTokenValid) {
                UserData.user = preferences.get { getString(USER_DATA, null)?.let { moshi.jsonToObject(it) } }
            } else {
                preferences.edit { remove(USER_DATA) }
            }

            splashDelay.await()

            _showSplashScreen.update { false }
        }
    }

    private fun checkAuthToken(): Boolean {
        val token = preferences.getString(AUTH_TOKEN, null) ?: return false

        return try {
            val jwt = JWT(token)
            val isValid = jwt.isExpired(10).not()
            return isValid
        } catch (e: Exception) {
            false
        }
    }
}
