package com.rafaelboban.activitytracker.di

import android.content.Context
import com.rafaelboban.activitytracker.tracking.ActivityTracker
import com.rafaelboban.activitytracker.tracking.LocationObserver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TrackerModule {

    @Provides
    @Singleton
    fun getLocationObserver(@ApplicationContext context: Context) = LocationObserver(context)

    @Provides
    @Singleton
    fun getActivityTracker(
        applicationScope: CoroutineScope,
        locationObserver: LocationObserver
    ) = ActivityTracker(applicationScope, locationObserver)
}
