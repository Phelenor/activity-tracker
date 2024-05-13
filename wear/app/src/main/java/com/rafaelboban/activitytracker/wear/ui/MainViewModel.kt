package com.rafaelboban.activitytracker.wear.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.core.shared.connectivity.connectors.WatchToPhoneConnector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val phoneConnector: WatchToPhoneConnector
) : ViewModel() {

    var showSplashScreen by mutableStateOf(true)
        private set

    fun showSplashScreen() {
        phoneConnector.connectedNode
            .filterNotNull()
            .onEach { node ->
                Timber.tag("CONNECTIVITY").i("Connected node = $node")
            }

        viewModelScope.launch {
            delay(200)
            showSplashScreen = false
        }
    }
}
