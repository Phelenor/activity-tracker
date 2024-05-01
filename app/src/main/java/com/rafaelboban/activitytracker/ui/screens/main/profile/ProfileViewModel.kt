package com.rafaelboban.activitytracker.ui.screens.main.profile

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.data.session.EncryptedSessionStorage
import com.rafaelboban.activitytracker.network.model.ChangeNameRequest
import com.rafaelboban.activitytracker.network.repository.UserRepository
import com.rafaelboban.activitytracker.util.UserData.user
import com.skydoves.sandwich.ApiResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    application: Application,
    private val userRepository: UserRepository,
    private val sessionStorage: EncryptedSessionStorage
) : AndroidViewModel(application) {

    var state by mutableStateOf(ProfileState(checkNotNull(user)))
        private set

    private val eventChannel = Channel<ProfileEvent>()
    val events = eventChannel.receiveAsFlow()

    fun logout() {
        viewModelScope.launch {
            user = null
            sessionStorage.clear()
            eventChannel.send(ProfileEvent.LogoutSuccess)
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            state = state.copy(submitInProgress = true)

            val response = userRepository.deleteAccount()

            if (response is ApiResponse.Success) {
                user = null
                sessionStorage.clear()
                eventChannel.send(ProfileEvent.DeleteAccountSuccess)
            } else {
                eventChannel.send(ProfileEvent.DeleteAccountError)
            }

            state = state.copy(submitInProgress = false)
        }
    }

    fun changeName(name: String) {
        viewModelScope.launch {
            state = state.copy(submitInProgress = true)

            val response = userRepository.changeName(ChangeNameRequest(name))

            if (response is ApiResponse.Success) {
                user = response.data
                state = state.copy(submitInProgress = false, user = response.data)
                sessionStorage.set(sessionStorage.get()?.copy(user = response.data))
                eventChannel.send(ProfileEvent.NameChangeSuccess)
            } else {
                eventChannel.send(ProfileEvent.NameChangeError)
            }
        }
    }

    fun dismissDialogs() {
        state = state.copy(
            showLogoutDialog = false,
            showChangeNameDialog = false,
            showDeleteAccountDialog = false
        )
    }

    fun showDialog(type: ProfileDialogType) {
        state = state.copy(
            showLogoutDialog = type == ProfileDialogType.SIGN_OUT,
            showChangeNameDialog = type == ProfileDialogType.CHANGE_NAME,
            showDeleteAccountDialog = type == ProfileDialogType.DELETE_ACCOUNT
        )
    }
}

enum class ProfileDialogType {
    SIGN_OUT, DELETE_ACCOUNT, CHANGE_NAME
}
