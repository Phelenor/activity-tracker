package com.rafaelboban.activitytracker.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.data.session.EncryptedSessionStorage
import com.rafaelboban.activitytracker.util.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionStorage: EncryptedSessionStorage
) : ViewModel() {

    var state by mutableStateOf(MainState())
        private set

    fun initAndSplashDelay() {
        viewModelScope.launch {
            val splashDelay = async { delay(200) }

            state = state.copy(isCheckingToken = true)
            state = state.copy(isLoggedIn = sessionStorage.get() != null)

            UserData.user = sessionStorage.get()?.user

            splashDelay.await()
            state = state.copy(isCheckingToken = false)
        }
    }
}
