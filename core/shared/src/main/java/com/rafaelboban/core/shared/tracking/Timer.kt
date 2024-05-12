package com.rafaelboban.core.shared.tracking

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object Timer {

    fun time(): Flow<Duration> {
        return flow {
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
}
