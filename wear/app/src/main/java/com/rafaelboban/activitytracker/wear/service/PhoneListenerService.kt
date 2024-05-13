package com.rafaelboban.activitytracker.wear.service

import android.content.Context
import android.os.PowerManager
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.rafaelboban.core.shared.connectivity.model.MessagingAction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class PhoneListenerService : WearableListenerService() {

    override fun onMessageReceived(message: MessageEvent) {
        val json = message.data.decodeToString()
        val action = Json.decodeFromString<MessagingAction>(json)

        if (action == MessagingAction.WakeUpWatch) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "activity-tracker:wake-lock")
            wakeLock.acquire(1.seconds.inWholeMilliseconds)
            wakeLock.release()
        }
    }
}
