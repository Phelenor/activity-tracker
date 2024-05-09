package com.rafaelboban.activitytracker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.rafaelboban.activitytracker.R
import com.rafaelboban.activitytracker.ui.MainActivity
import com.rafaelboban.core.tracker.utils.ActivityDataFormatter
import com.rafaelboban.core.tracker.utils.ActivityDataFormatter.formatElapsedTimeDisplay
import com.rafaelboban.core.tracker.tracking.ActivityTracker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import javax.inject.Inject

@AndroidEntryPoint
class ActivityTrackerService : LifecycleService() {

    @Inject
    lateinit var tracker: ActivityTracker

    private val notificationManager by lazy { checkNotNull(getSystemService<NotificationManager>()) }

    private val notificationBuilder by lazy {
        NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo_main)
            .setContentTitle(getString(R.string.now_running))
    }

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
        createNotificationChannel()

        val activityIntent = Intent(applicationContext, MainActivity::class.java).apply {
            data = "activity_tracker://current_activity".toUri()
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(activityIntent)
            getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
        }

        val notification = notificationBuilder
            .setContentText("00:00")
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        startNotificationUpdates()
    }

    private fun stop() {
        isActive = false
        lifecycleScope.cancel()
        stopSelf()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )

        notificationManager.createNotificationChannel(channel)
    }

    private fun startNotificationUpdates() {
        combine(
            tracker.duration,
            tracker.activityData
        ) { duration, data ->
            val distanceUnit = if (data.distanceMeters < 1000) "m" else "km"
            notificationBuilder.setContentText("${duration.formatElapsedTimeDisplay()} | ${ActivityDataFormatter.formatDistanceDisplay(data.distanceMeters)} $distanceUnit")
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        }.launchIn(lifecycleScope)
    }

    companion object {

        var isActive = false
            private set

        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL_ID = "activity_tracker"
        private const val NOTIFICATION_CHANNEL_NAME = "Activity Tracker"

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
