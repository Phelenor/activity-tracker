package com.rafaelboban.activitytracker.ui.screens.main.profile

import android.app.Application
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.di.PreferencesEncrypted
import com.rafaelboban.activitytracker.model.network.PostStatus
import com.rafaelboban.activitytracker.network.model.ChangeNameRequest
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
class ProfileViewModel @Inject constructor(
    application: Application,
    private val userRepository: UserRepository,
    private val moshi: Moshi,
    @PreferencesEncrypted private val preferences: SharedPreferences
) : AndroidViewModel(application) {

    var user by mutableStateOf(checkNotNull(UserData.user))

    var changeNamePostStatus by mutableStateOf<PostStatus?>(null)
    var deleteAccountPostStatus by mutableStateOf<PostStatus?>(null)

    fun logout() {
        UserData.user = null

        preferences.edit {
            remove(USER_DATA)
            remove(AUTH_TOKEN)
            remove(USER_ID)
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            deleteAccountPostStatus = PostStatus.IN_PROGRESS
            userRepository.deleteAccount()
            deleteAccountPostStatus = PostStatus.SUCCESS
        }
    }

    fun changeName(name: String) {
        viewModelScope.launch {
            changeNamePostStatus = PostStatus.IN_PROGRESS

            userRepository.changeName(ChangeNameRequest(name)).onSuccess {
                user = data
                UserData.user = data
                preferences.edit { putString(USER_DATA, moshi.objectToJson(data)) }
                changeNamePostStatus = PostStatus.SUCCESS
            }.onFailure {
                changeNamePostStatus = PostStatus.ERROR
            }
        }
    }
}
