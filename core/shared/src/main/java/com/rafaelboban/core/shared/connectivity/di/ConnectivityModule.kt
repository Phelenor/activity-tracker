package com.rafaelboban.core.shared.connectivity.di

import android.content.Context
import com.rafaelboban.core.shared.connectivity.clients.WearMessagingClient
import com.rafaelboban.core.shared.connectivity.clients.WearNodeDiscovery
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConnectivityModule {

    @Provides
    @Singleton
    fun provideNodeDiscovery(@ApplicationContext context: Context) = WearNodeDiscovery(context)

    @Provides
    @Singleton
    fun provideWearMessagingClient(@ApplicationContext context: Context) = WearMessagingClient(context)
}
