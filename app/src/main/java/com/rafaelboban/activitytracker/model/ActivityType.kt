package com.rafaelboban.activitytracker.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.rafaelboban.activitytracker.R

enum class ActivityType(@StringRes val titleRes: Int, @DrawableRes val drawableRes: Int) {
    RUN(R.string.running, R.drawable.ic_run),
    WALK(R.string.walking, R.drawable.ic_walk),
    CYCLING(R.string.cycling, R.drawable.ic_bike),
    OTHER(R.string.other, R.drawable.ic_accessibility)
}
