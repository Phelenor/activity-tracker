package com.rafaelboban.activitytracker.model.ui

import kotlin.time.Duration

sealed interface ActivityUserStatus {
    data object Joined : ActivityUserStatus
    data object Connected : ActivityUserStatus
    data object Active : ActivityUserStatus
    data class Finished(val duration: Duration) : ActivityUserStatus
}
