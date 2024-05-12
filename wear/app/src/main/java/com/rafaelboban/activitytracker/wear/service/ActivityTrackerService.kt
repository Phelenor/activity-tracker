package com.rafaelboban.activitytracker.wear.service

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.rafaelboban.activitytracker.wear.tracker.ActivityTracker
import com.rafaelboban.core.shared.model.ActivityStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ActivityTrackerService : LifecycleService() {

    @Inject
    lateinit var tracker: ActivityTracker

    @Inject
    lateinit var notificationManager: ActivityOngoingNotificationManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }

        return START_STICKY
    }

    private fun start() {
        if (isActive) return

        isActive = true

        notificationManager.createNotificationChannel()

        startForeground(NOTIFICATION_ID, notificationManager.buildNotification())
        startNotificationUpdates()

        Timber.i("Tracker Service started.")
    }

    private fun stop() {
        isActive = false
        lifecycleScope.cancel()
        stopSelf()

        Timber.i("Tracker Service stopped.")
    }

    private fun startNotificationUpdates() {
        tracker.duration
            .combine(tracker.activityStatus) { duration, status ->
                notificationManager.updateNotification(duration, isPaused = status == ActivityStatus.PAUSED)
            }.launchIn(lifecycleScope)
    }

    companion object {

        var isActive = false
            private set

        private const val NOTIFICATION_ID = 1

        private const val ACTION_START = "ACTION_START"
        private const val ACTION_STOP = "ACTION_STOP"

        fun createStartIntent(context: Context) = Intent(context, ActivityTrackerService::class.java).apply {
            action = ACTION_START
        }

        fun createStopIntent(context: Context) = Intent(context, ActivityTrackerService::class.java).apply {
            action = ACTION_STOP
        }
    }
}
