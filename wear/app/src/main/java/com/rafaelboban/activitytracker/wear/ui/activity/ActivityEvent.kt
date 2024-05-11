package com.rafaelboban.activitytracker.wear.ui.activity

import com.rafaelboban.core.shared.ui.util.UiText

sealed interface ActivityEvent {

    data class Error(val message: UiText): ActivityEvent
}
