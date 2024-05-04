package com.rafaelboban.activitytracker.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

inline fun Context.editPreferences(block: SharedPreferences.Editor.() -> SharedPreferences.Editor) = PreferenceManager.getDefaultSharedPreferences(this).edit().block().apply()

inline fun <T> Context.getPreference(block: SharedPreferences.() -> T): T = PreferenceManager.getDefaultSharedPreferences(this).block()

inline fun SharedPreferences.edit(block: SharedPreferences.Editor.() -> SharedPreferences.Editor) = this.edit().block().apply()

inline fun <T> SharedPreferences.get(block: SharedPreferences.() -> T): T = this.block()

fun Context.getActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}
