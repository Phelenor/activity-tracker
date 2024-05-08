package com.rafaelboban.core.tracker.model

enum class ActivityStatus {
    NOT_STARTED, IN_PROGRESS, PAUSED, FINISHED;

    companion object {
        val ActivityStatus.isRunning: Boolean
            get() = this == IN_PROGRESS || this == PAUSED
    }
}
