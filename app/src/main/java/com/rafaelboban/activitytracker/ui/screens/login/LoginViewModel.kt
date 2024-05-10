package com.rafaelboban.activitytracker.ui.screens.login

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.R
import com.rafaelboban.activitytracker.data.session.AuthInfo
import com.rafaelboban.activitytracker.data.session.EncryptedSessionStorage
import com.rafaelboban.activitytracker.network.model.LoginRequest
import com.rafaelboban.activitytracker.network.repository.UserRepository
import com.rafaelboban.activitytracker.ui.util.UiText
import com.rafaelboban.activitytracker.util.UserData
import com.skydoves.sandwich.message
import com.skydoves.sandwich.suspendOnError
import com.skydoves.sandwich.suspendOnException
import com.skydoves.sandwich.suspendOnSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionStorage: EncryptedSessionStorage
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    private val eventChannel = Channel<LoginEvent>()
    val events = eventChannel.receiveAsFlow()

    fun login(idToken: String, nonce: String) {
        viewModelScope.launch {
            userRepository.login(LoginRequest(idToken, nonce)).suspendOnSuccess {
                UserData.user = data.user
                sessionStorage.set(AuthInfo(data.user, data.accessToken, data.refreshToken))

                isLoading = false
                eventChannel.send(LoginEvent.Success)
            }.suspendOnError {
                isLoading = false
                eventChannel.send(LoginEvent.Error(UiText.DynamicString(message())))
            }.suspendOnException {
                isLoading = false
                eventChannel.send(LoginEvent.Error(UiText.StringResource(R.string.login_error)))
            }
        }
    }

    fun startGoogleLogin() {
        isLoading = true
    }

    fun finishGoogleLogin(showMessage: Boolean) {
        viewModelScope.launch {
            isLoading = false
            if (showMessage) {
                eventChannel.send(LoginEvent.Error(UiText.StringResource(R.string.google_login_error)))
            }
        }
    }
}
