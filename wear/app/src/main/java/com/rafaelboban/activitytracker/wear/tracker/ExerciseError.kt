package com.rafaelboban.activitytracker.wear.tracker

import com.rafaelboban.core.tracker.utils.Error

enum class ExerciseError : Error {
    TRACKING_NOT_SUPPORTED,
    ONGOING_OWN_EXERCISE,
    ONGOING_OTHER_EXERCISE,
    EXERCISE_ALREADY_ENDED,
    UNKNOWN
}
