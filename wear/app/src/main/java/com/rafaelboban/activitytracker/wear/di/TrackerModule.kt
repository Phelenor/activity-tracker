package com.rafaelboban.activitytracker.wear.di

import android.content.Context
import com.rafaelboban.activitytracker.wear.tracker.HealthServicesExerciseTracker
import com.rafaelboban.core.shared.connectivity.clients.WearMessagingClient
import com.rafaelboban.core.shared.connectivity.clients.WearNodeDiscovery
import com.rafaelboban.core.shared.connectivity.connectors.WatchToPhoneConnector
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
    fun getExerciseTracker(@ApplicationContext context: Context) = HealthServicesExerciseTracker(context)

    @Provides
    @Singleton
    fun getWatchToPhoneConnector(
        applicationScope: CoroutineScope,
        messagingClient: WearMessagingClient,
        nodeDiscovery: WearNodeDiscovery
    ) = WatchToPhoneConnector(applicationScope, nodeDiscovery, messagingClient)
}
