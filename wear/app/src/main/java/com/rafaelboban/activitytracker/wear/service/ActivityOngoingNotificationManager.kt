package com.rafaelboban.activitytracker.wear.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.rafaelboban.activitytracker.wear.R
import com.rafaelboban.activitytracker.wear.ui.MainActivity
import com.rafaelboban.core.shared.model.ActivityType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.time.Duration

class ActivityOngoingNotificationManager @Inject constructor(
    @ApplicationContext val context: Context
) {
    private val manager by lazy { context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager }

    private val notificationBuilder by lazy {
        NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(com.rafaelboban.core.shared.R.drawable.app_logo_main)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }

    fun createNotificationChannel() {
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )

        manager.createNotificationChannel(notificationChannel)
    }

    fun buildNotification(activityType: ActivityType): Notification {
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            data = "activity_tracker://current_activity".toUri()
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(activityIntent)
            getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val builder = notificationBuilder
            .setContentIntent(pendingIntent)
            .setContentTitle(context.getString(activityType.titleRes))

        val startMillis = SystemClock.elapsedRealtime()
        val ongoingActivityStatus = Status.Builder()
            .addTemplate(ONGOING_STATUS_TEMPLATE)
            .addPart("duration", Status.StopwatchPart(startMillis))
            .build()

        val ongoingActivity = OngoingActivity.Builder(context, NOTIFICATION_ID, builder)
            .setAnimatedIcon(com.rafaelboban.core.shared.R.drawable.app_logo_main)
            .setStaticIcon(com.rafaelboban.core.shared.R.drawable.app_logo_main)
            .setTouchIntent(pendingIntent)
            .setStatus(ongoingActivityStatus)
            .setOngoingActivityId(ONGOING_ACTIVITY_ID)
            .build()

        ongoingActivity.apply(context)

        return builder.build()
    }

    fun updateNotification(duration: Duration, isPaused: Boolean = false) {
        val startMillis = SystemClock.elapsedRealtime() - duration.inWholeMilliseconds

        val statusPart = if (isPaused) Status.TextPart(context.getString(R.string.activity_paused)) else Status.StopwatchPart(startMillis)
        val ongoingActivityStatus = Status.Builder()
            .addTemplate(ONGOING_STATUS_TEMPLATE)
            .addPart("duration", statusPart)
            .build()

        OngoingActivity.recoverOngoingActivity(context, ONGOING_ACTIVITY_ID)?.update(context, ongoingActivityStatus)
    }

    companion object {

        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL_ID = "activity_tracker"
        private const val NOTIFICATION_CHANNEL_NAME = "Activity Tracker"

        private const val ONGOING_STATUS_TEMPLATE = "#duration#"
        private const val ONGOING_ACTIVITY_ID = 14231212
    }
}
