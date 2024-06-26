package com.rafaelboban.core.shared.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.rafaelboban.core.shared.R
import kotlinx.serialization.Serializable

@Serializable
enum class ActivityType(
    @StringRes val nameRes: Int,
    @StringRes val inProgressTitleRes: Int,
    @StringRes val singularRes: Int,
    @DrawableRes val drawableRes: Int,
    val showPace: Boolean = false,
    val maxSpeed: Float,
    val minSpeed: Float
) {
    RUN(R.string.running, R.string.now_running, R.string.run, R.drawable.ic_run, true, 17f, 8f),
    WALK(R.string.walking, R.string.now_walking, R.string.walk, R.drawable.ic_walk, true, 7f, 4f),
    CYCLING(R.string.cycling, R.string.now_cycling, R.string.ride, R.drawable.ic_bike, false, 35f, 18f),
    OTHER(R.string.other, R.string.now_exercising, R.string.exercise, R.drawable.ic_accessibility, false, Float.MAX_VALUE, Float.MIN_VALUE)
}
