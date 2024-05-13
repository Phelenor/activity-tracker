package com.rafaelboban.activitytracker.wear.tracker

import androidx.health.services.client.data.ExerciseType
import com.rafaelboban.core.shared.model.ActivityType

fun ActivityType.toExerciseType() = when (this) {
    ActivityType.RUN -> ExerciseType.RUNNING
    ActivityType.WALK -> ExerciseType.WALKING
    ActivityType.CYCLING -> ExerciseType.BIKING
    ActivityType.OTHER -> ExerciseType.WORKOUT
}
