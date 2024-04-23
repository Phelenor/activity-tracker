package com.rafaelboban.activitytracker.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.network.repository.UserRepository
import com.rafaelboban.activitytracker.network.model.LoginRequest
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
                Timber.tag("MARIN").d(data.toString())
                Timber.tag("MARIN").d(headers.toString())
            }
        }
    }

    fun test() {
        viewModelScope.launch {
            userRepository.test().onSuccess {
                Timber.tag("MARIN").d(headers.toString())
            }
        }
    }
}
