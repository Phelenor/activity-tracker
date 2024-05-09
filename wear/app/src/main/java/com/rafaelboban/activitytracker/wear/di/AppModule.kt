package com.rafaelboban.activitytracker.wear.di

import android.content.Context
import com.rafaelboban.activitytracker.wear.ActivityTrackerWearApplication
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providerApplicationContext(@ApplicationContext context: Context) = (context as ActivityTrackerWearApplication).applicationScope
}
