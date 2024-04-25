package com.rafaelboban.activitytracker.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PreferencesStandard

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PreferencesEncrypted
