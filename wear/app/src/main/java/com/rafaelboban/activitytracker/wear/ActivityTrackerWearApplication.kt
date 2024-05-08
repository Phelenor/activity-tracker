package com.rafaelboban.activitytracker.wear

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@HiltAndroidApp
class ActivityTrackerWearApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())
}
