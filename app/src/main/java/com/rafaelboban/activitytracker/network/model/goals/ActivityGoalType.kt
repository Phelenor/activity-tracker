package com.rafaelboban.activitytracker.network.model.goals

import androidx.annotation.StringRes
import com.rafaelboban.activitytracker.R

const val PREFERENCE_SHOW_GOALS_REMINDER = "PREFERENCE_SHOW_GOALS_REMINDER"

enum class ActivityGoalType(@StringRes val stringRes: Int) {
    DISTANCE(R.string.distance),
    DURATION(R.string.duration),
    CALORIES(R.string.calories),
    AVG_HEART_RATE(R.string.avg_heartrate),
    AVG_SPEED(R.string.avg_speed),
    AVG_PACE(R.string.avg_pace),
    IN_HR_ZONE(R.string.duration_pct_in_zone),
    BELOW_ABOVE_HR_ZONE(R.string.duration_pct_above_below_zone)
}
