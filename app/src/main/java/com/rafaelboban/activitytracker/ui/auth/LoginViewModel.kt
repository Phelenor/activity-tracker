package com.rafaelboban.activitytracker.ui.auth

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.network.model.LoginRequest
import com.rafaelboban.activitytracker.network.repository.UserRepository
import com.rafaelboban.activitytracker.util.Constants.AUTH_TOKEN
import com.rafaelboban.activitytracker.util.Constants.USER_ID
import com.rafaelboban.activitytracker.util.UserData
import com.rafaelboban.activitytracker.util.editPreferences
import com.skydoves.sandwich.onFailure
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

    var loginState by mutableStateOf(LoginState.IDLE)

    fun login(idToken: String, nonce: String) {
        viewModelScope.launch {
            userRepository.login(LoginRequest(idToken, nonce)).onSuccess {
                loginState = LoginState.SUCCESS

                UserData.user = data.user

                getApplication<Application>().editPreferences {
                    putString(AUTH_TOKEN, data.token)
                    putString(USER_ID, data.user.id)
                }
            }.onFailure {
                loginState = LoginState.IDLE
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

    enum class LoginState {
        IDLE, IN_PROGRESS, SUCCESS
    }
}
