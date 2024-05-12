package com.rafaelboban.core.shared.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.rafaelboban.core.shared.R
import kotlinx.serialization.Serializable

@Serializable
enum class ActivityType(@StringRes val nameRes: Int, @StringRes val titleRes: Int, @DrawableRes val drawableRes: Int) {
    RUN(R.string.running, R.string.now_running, R.drawable.ic_run),
    WALK(R.string.walking, R.string.now_walking, R.drawable.ic_walk),
    CYCLING(R.string.cycling, R.string.now_cycling, R.drawable.ic_bike),
    OTHER(R.string.other, R.string.now_exercising, R.drawable.ic_accessibility)
}
