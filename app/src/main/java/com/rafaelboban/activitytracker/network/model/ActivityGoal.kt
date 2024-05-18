package com.rafaelboban.activitytracker.network.model

import androidx.annotation.StringRes
import com.rafaelboban.activitytracker.R

const val PREFERENCE_SHOW_GOALS_REMINDER = "PREFERENCE_SHOW_GOALS_REMINDER"

enum class ActivityGoal(@StringRes val stringRes: Int) {
    DISTANCE(R.string.distance),
    DURATION(R.string.duration),
    CALORIES(R.string.calories),
    AVG_SPEED(R.string.average_speed),
    HR_ZONE(R.string.duration_pct_in_zone)
}
