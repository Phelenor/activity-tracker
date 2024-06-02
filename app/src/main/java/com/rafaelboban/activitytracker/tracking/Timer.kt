package com.rafaelboban.activitytracker.tracking

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import java.time.Instant
import kotlin.time.Duration.Companion.milliseconds

object Timer {

    fun time() = flow {
        var timerTime = Instant.now().toEpochMilli()

        while (true) {
            delay(500)
            val currentTime = Instant.now().toEpochMilli()
            val elapsedTime = currentTime - timerTime
            emit(elapsedTime.milliseconds)
            timerTime = currentTime
        }
    }
}
