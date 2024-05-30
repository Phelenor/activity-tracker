package com.rafaelboban.activitytracker.wear.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.rafaelboban.activitytracker.wear.ui.MainActivity
import com.rafaelboban.core.shared.connectivity.model.MessagingAction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class PhoneListenerService : WearableListenerService() {

    @SuppressLint("WearRecents")
    override fun onMessageReceived(message: MessageEvent) {
        val json = message.data.decodeToString()
        val action = Json.decodeFromString<MessagingAction>(json)

        when (action) {
            MessagingAction.WakeUpWatch -> {
                val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                val wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "activity-tracker:wake-lock")
                wakeLock.acquire(1.seconds.inWholeMilliseconds)
                wakeLock.release()
            }

            MessagingAction.ConnectionRequest -> {
                Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }.also { intent ->
                    startActivity(intent)
                }
            }

            else -> Unit
        }
    }
}
