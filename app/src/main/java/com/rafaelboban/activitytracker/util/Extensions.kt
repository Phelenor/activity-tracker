package com.rafaelboban.activitytracker.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

inline fun Context.editPreferences(block: SharedPreferences.Editor.() -> SharedPreferences.Editor) = PreferenceManager.getDefaultSharedPreferences(this).edit().block().apply()

inline fun <T> Context.getPreference(block: SharedPreferences.() -> T): T =  PreferenceManager.getDefaultSharedPreferences(this).block()
