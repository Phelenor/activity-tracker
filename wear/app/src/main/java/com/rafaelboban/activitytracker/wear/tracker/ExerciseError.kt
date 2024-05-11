package com.rafaelboban.activitytracker.wear.tracker

import com.rafaelboban.activitytracker.wear.R
import com.rafaelboban.core.shared.ui.util.UiText
import com.rafaelboban.core.shared.utils.Error

enum class ExerciseError : Error {
    TRACKING_NOT_SUPPORTED,
    ONGOING_OWN_EXERCISE,
    ONGOING_OTHER_EXERCISE,
    EXERCISE_ALREADY_ENDED,
    UNKNOWN
}

fun ExerciseError.toUiText(): UiText? {
    return when(this) {
        ExerciseError.ONGOING_OWN_EXERCISE,
        ExerciseError.ONGOING_OTHER_EXERCISE -> UiText.StringResource(R.string.error_ongoing_exercise)
        ExerciseError.EXERCISE_ALREADY_ENDED -> UiText.StringResource(R.string.error_exercise_already_ended)
        ExerciseError.UNKNOWN -> UiText.StringResource(R.string.unknown_error)
        ExerciseError.TRACKING_NOT_SUPPORTED -> null
    }
}
