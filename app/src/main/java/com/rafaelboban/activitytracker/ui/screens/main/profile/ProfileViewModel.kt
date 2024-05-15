package com.rafaelboban.activitytracker.ui.screens.main.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.data.session.EncryptedSessionStorage
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
    private val userRepository: UserRepository,
    private val sessionStorage: EncryptedSessionStorage
) : ViewModel() {

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

    fun updateUser(
        name: String? = null,
        height: Int? = null,
        weight: Int? = null,
        birthTimestamp: Long? = null
    ) {
        viewModelScope.launch {
            state = state.copy(submitInProgress = true)

            val response = userRepository.updateUserData(name, height, weight, birthTimestamp)

            if (response is ApiResponse.Success) {
                user = response.data
                state = state.copy(submitInProgress = false, user = response.data)
                sessionStorage.set(sessionStorage.get()?.copy(user = response.data))
                eventChannel.send(ProfileEvent.UserInfoChangeSuccess)
            } else {
                eventChannel.send(ProfileEvent.UserInfoChangeError)
            }
        }
    }

    fun dismissDialogs() {
        state = state.copy(
            showLogoutDialog = false,
            showChangeNameDialog = false,
            showDeleteAccountDialog = false,
            showWeightDialog = false,
            showHeightDialog = false,
            showBirthDateDialog = false
        )
    }

    fun showDialog(type: ProfileDialogType) {
        state = state.copy(
            showLogoutDialog = type == ProfileDialogType.SIGN_OUT,
            showChangeNameDialog = type == ProfileDialogType.CHANGE_NAME,
            showDeleteAccountDialog = type == ProfileDialogType.DELETE_ACCOUNT,
            showWeightDialog = type == ProfileDialogType.UPDATE_WEIGHT,
            showHeightDialog = type == ProfileDialogType.UPDATE_HEIGHT,
            showBirthDateDialog = type == ProfileDialogType.UPDATE_AGE
        )
    }
}

enum class ProfileDialogType {
    SIGN_OUT, DELETE_ACCOUNT, CHANGE_NAME, UPDATE_WEIGHT, UPDATE_HEIGHT, UPDATE_AGE
}
