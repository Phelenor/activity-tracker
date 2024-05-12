package com.rafaelboban.activitytracker.service

import android.content.Intent
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.rafaelboban.activitytracker.ui.MainActivity
import com.rafaelboban.core.shared.connectivity.model.MessagingAction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json

@AndroidEntryPoint
class WatchListenerService : WearableListenerService() {

    override fun onMessageReceived(message: MessageEvent) {
        val json = message.data.decodeToString()
        val action = Json.decodeFromString<MessagingAction>(json)

        if (action == MessagingAction.OpenAppOnPhone) {
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }.also { intent ->
                startActivity(intent)
            }
        }
    }
}
