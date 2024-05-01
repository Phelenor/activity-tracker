package com.rafaelboban.activitytracker.ui.screens.main.profile

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.data.session.EncryptedSessionStorage
import com.rafaelboban.activitytracker.model.network.PostStatus
import com.rafaelboban.activitytracker.network.model.ChangeNameRequest
import com.rafaelboban.activitytracker.network.repository.UserRepository
import com.rafaelboban.activitytracker.util.UserData
import com.skydoves.sandwich.onFailure
import com.skydoves.sandwich.suspendOnSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    application: Application,
    private val userRepository: UserRepository,
    private val sessionStorage: EncryptedSessionStorage
) : AndroidViewModel(application) {

    var user by mutableStateOf(checkNotNull(UserData.user))

    var changeNamePostStatus by mutableStateOf<PostStatus?>(null)
    var deleteAccountPostStatus by mutableStateOf<PostStatus?>(null)

    fun logout() {
        viewModelScope.launch {
            UserData.user = null
            sessionStorage.clear()
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

            userRepository.changeName(ChangeNameRequest(name)).suspendOnSuccess {
                user = data
                UserData.user = data
                sessionStorage.set(sessionStorage.get()?.copy(user = data))
                changeNamePostStatus = PostStatus.SUCCESS
            }.onFailure {
                changeNamePostStatus = PostStatus.ERROR
            }
        }
    }
}
