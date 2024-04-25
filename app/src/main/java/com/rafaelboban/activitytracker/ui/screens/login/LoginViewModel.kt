package com.rafaelboban.activitytracker.ui.screens.login

import android.app.Application
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.di.PreferencesEncrypted
import com.rafaelboban.activitytracker.model.network.PostStatus
import com.rafaelboban.activitytracker.network.model.LoginRequest
import com.rafaelboban.activitytracker.network.repository.UserRepository
import com.rafaelboban.activitytracker.util.Constants.AUTH_TOKEN
import com.rafaelboban.activitytracker.util.Constants.USER_DATA
import com.rafaelboban.activitytracker.util.Constants.USER_ID
import com.rafaelboban.activitytracker.util.UserData
import com.rafaelboban.activitytracker.util.edit
import com.rafaelboban.activitytracker.util.objectToJson
import com.skydoves.sandwich.onFailure
import com.skydoves.sandwich.onSuccess
import com.squareup.moshi.Moshi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    application: Application,
    private val userRepository: UserRepository,
    private val moshi: Moshi,
    @PreferencesEncrypted private val preferences: SharedPreferences
) : AndroidViewModel(application) {

    var postStatus by mutableStateOf<PostStatus?>(null)

    fun login(idToken: String, nonce: String) {
        viewModelScope.launch {
            userRepository.login(LoginRequest(idToken, nonce)).onSuccess {
                postStatus = PostStatus.SUCCESS

                UserData.user = data.user

                preferences.edit {
                    putString(USER_DATA, moshi.objectToJson(data.user))
                    putString(AUTH_TOKEN, data.token)
                    putString(USER_ID, data.user.id)
                }
            }.onFailure {
                postStatus = null
            }
        }
    }
}
