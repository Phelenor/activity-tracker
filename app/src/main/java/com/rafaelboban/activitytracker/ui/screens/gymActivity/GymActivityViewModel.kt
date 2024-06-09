package com.rafaelboban.activitytracker.ui.screens.gymActivity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.rafaelboban.activitytracker.network.repository.GymRepository
import com.rafaelboban.activitytracker.ui.screens.login.LoginEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class GymActivityViewModel @Inject constructor(
    private val gymRepository: GymRepository
) : ViewModel() {

    var state by mutableStateOf(GymActivityState())
        private set

    private val eventChannel = Channel<GymActivityEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: GymActivityAction) {
        when (action) {
            GymActivityAction.DiscardActivity -> TODO()
            GymActivityAction.DismissDialogs -> TODO()
            GymActivityAction.OnBackClick -> TODO()
            GymActivityAction.OnFinishClick -> TODO()
            GymActivityAction.OnPauseClick -> TODO()
            GymActivityAction.OnResumeClick -> TODO()
            GymActivityAction.OnStartClick -> TODO()
            GymActivityAction.RetryGymActivityFetch -> TODO()
            GymActivityAction.SaveActivity -> TODO()
        }
    }
}
