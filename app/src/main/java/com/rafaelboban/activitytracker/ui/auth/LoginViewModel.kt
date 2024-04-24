package com.rafaelboban.activitytracker.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.network.model.LoginRequest
import com.rafaelboban.activitytracker.network.repository.UserRepository
import com.rafaelboban.activitytracker.util.Constants.AUTH_TOKEN
import com.rafaelboban.activitytracker.util.Constants.USER_ID
import com.rafaelboban.activitytracker.util.UserData
import com.rafaelboban.activitytracker.util.editPreferences
import com.skydoves.sandwich.onSuccess
import com.skydoves.sandwich.retrofit.headers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    application: Application,
    private val userRepository: UserRepository
) : AndroidViewModel(application) {

    fun login(idToken: String, nonce: String) {
        viewModelScope.launch {
            userRepository.login(LoginRequest(idToken, nonce)).onSuccess {
                UserData.user = data.user

                getApplication<Application>().editPreferences {
                    putString(AUTH_TOKEN, data.token)
                    putString(USER_ID, data.user.id)
                }
            }
        }
    }

    fun ping() {
        viewModelScope.launch {
            userRepository.ping().onSuccess {
                Timber.tag("MARIN").d(headers.toString())
            }
        }
    }
}
