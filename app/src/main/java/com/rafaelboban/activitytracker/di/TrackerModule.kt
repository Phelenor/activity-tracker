package com.rafaelboban.activitytracker.di

import android.content.Context
import com.rafaelboban.activitytracker.network.ws.WebSocketClient
import com.rafaelboban.activitytracker.tracking.ActivityTracker
import com.rafaelboban.activitytracker.tracking.GroupActivityDataService
import com.rafaelboban.activitytracker.tracking.GymActivityDataService
import com.rafaelboban.activitytracker.tracking.LocationObserver
import com.rafaelboban.core.shared.connectivity.clients.WearMessagingClient
import com.rafaelboban.core.shared.connectivity.clients.WearNodeDiscovery
import com.rafaelboban.core.shared.connectivity.connectors.PhoneToWatchConnector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
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
        locationObserver: LocationObserver,
        watchConnector: PhoneToWatchConnector
    ) = ActivityTracker(applicationScope, locationObserver, watchConnector)

    @Provides
    @Singleton
    fun getPhoneToWatchConnector(
        applicationScope: CoroutineScope,
        messagingClient: WearMessagingClient,
        nodeDiscovery: WearNodeDiscovery
    ) = PhoneToWatchConnector(applicationScope, nodeDiscovery, messagingClient)

    @Provides
    @Singleton
    fun getGroupActivityDataService(
        tracker: ActivityTracker,
        webSocketClient: WebSocketClient,
        applicationScope: CoroutineScope
    ) = GroupActivityDataService(tracker, webSocketClient, applicationScope)

    @Provides
    @Singleton
    fun getGymActivityDataService(
        webSocketClient: WebSocketClient,
        applicationScope: CoroutineScope,
        json: Json
    ) = GymActivityDataService(webSocketClient, applicationScope, json)
}
