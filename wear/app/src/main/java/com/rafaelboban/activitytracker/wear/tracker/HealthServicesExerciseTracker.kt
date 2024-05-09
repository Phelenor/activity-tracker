package com.rafaelboban.activitytracker.wear.tracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServices
import androidx.health.services.client.HealthServicesException
import androidx.health.services.client.clearUpdateCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseTrackedStatus
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.WarmUpConfig
import androidx.health.services.client.endExercise
import androidx.health.services.client.getCapabilities
import androidx.health.services.client.getCurrentExerciseInfo
import androidx.health.services.client.pauseExercise
import androidx.health.services.client.prepareExercise
import androidx.health.services.client.resumeExercise
import androidx.health.services.client.startExercise
import com.rafaelboban.core.tracker.utils.EmptyResult
import com.rafaelboban.core.tracker.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import kotlin.math.roundToInt

class HealthServicesExerciseTracker(private val context: Context) {

    private val client = HealthServices.getClient(context).exerciseClient

    private var exerciseType = ExerciseType.RUNNING

    val heartRate: Flow<Int>
        get() = callbackFlow {
            val callback = object : ExerciseUpdateCallback {

                override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
                    val heartRates = update.latestMetrics.getData(DataType.HEART_RATE_BPM)
                    val currentHeartRate = heartRates.firstOrNull()?.value

                    currentHeartRate?.let {
                        trySend(currentHeartRate.roundToInt())
                    }
                }

                override fun onAvailabilityChanged(dataType: DataType<*, *>, availability: Availability) = Unit

                override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) = Unit

                override fun onRegistered() {
                    Timber.i("Health Services registered.")
                }

                override fun onRegistrationFailed(throwable: Throwable) {
                    Timber.e(throwable.message)
                }
            }

            client.setUpdateCallback(callback)

            awaitClose {
                runBlocking {
                    client.clearUpdateCallback(callback)
                }
            }
        }.flowOn(Dispatchers.IO)

    suspend fun isHeartRateTrackingSupported(): Boolean {
        return hasBodySensorsPermission() && runCatching {
            val capabilities = client.getCapabilities()
            val supportedDataTypes = capabilities
                .typeToCapabilities[exerciseType]
                ?.supportedDataTypes ?: setOf()

            DataType.HEART_RATE_BPM in supportedDataTypes
        }.getOrDefault(false)
    }

    suspend fun prepareExercise(): EmptyResult<ExerciseError> {
        if (isHeartRateTrackingSupported().not()) {
            return Result.Error(ExerciseError.TRACKING_NOT_SUPPORTED)
        }

        val result = getActiveExerciseInfo()
        if (result is Result.Error) {
            return result
        }

        val config = WarmUpConfig(
            exerciseType = exerciseType,
            dataTypes = setOf(DataType.HEART_RATE_BPM)
        )

        client.prepareExercise(config)

        return Result.Success(Unit)
    }

    suspend fun startExercise(): EmptyResult<ExerciseError> {
        if (isHeartRateTrackingSupported().not()) {
            return Result.Error(ExerciseError.TRACKING_NOT_SUPPORTED)
        }

        val result = getActiveExerciseInfo()
        if (result is Result.Error) {
            return result
        }

        val config = ExerciseConfig.builder(exerciseType)
            .setDataTypes(setOf(DataType.HEART_RATE_BPM))
            .setIsAutoPauseAndResumeEnabled(false)
            .build()

        client.startExercise(config)

        return Result.Success(Unit)
    }

    suspend fun resumeExercise(): EmptyResult<ExerciseError> {
        if (isHeartRateTrackingSupported().not()) {
            return Result.Error(ExerciseError.TRACKING_NOT_SUPPORTED)
        }

        val result = getActiveExerciseInfo()
        if (result is Result.Error && result.error == ExerciseError.ONGOING_OTHER_EXERCISE) {
            return result
        }

        return try {
            client.resumeExercise()
            Result.Success(Unit)
        } catch (e: HealthServicesException) {
            Result.Error(ExerciseError.EXERCISE_ALREADY_ENDED)
        }
    }

    suspend fun pauseExercise(): EmptyResult<ExerciseError> {
        if (isHeartRateTrackingSupported().not()) {
            return Result.Error(ExerciseError.TRACKING_NOT_SUPPORTED)
        }

        val result = getActiveExerciseInfo()
        if (result is Result.Error && result.error == ExerciseError.ONGOING_OTHER_EXERCISE) {
            return result
        }

        return try {
            client.pauseExercise()
            Result.Success(Unit)
        } catch (e: HealthServicesException) {
            Result.Error(ExerciseError.EXERCISE_ALREADY_ENDED)
        }
    }

    suspend fun stopExercise(): EmptyResult<ExerciseError> {
        if (isHeartRateTrackingSupported().not()) {
            return Result.Error(ExerciseError.TRACKING_NOT_SUPPORTED)
        }

        val result = getActiveExerciseInfo()
        if (result is Result.Error && result.error == ExerciseError.ONGOING_OTHER_EXERCISE) {
            return result
        }

        return try {
            client.endExercise()
            Result.Success(Unit)
        } catch (e: HealthServicesException) {
            Result.Error(ExerciseError.EXERCISE_ALREADY_ENDED)
        }
    }

    @SuppressLint("RestrictedApi")
    private suspend fun getActiveExerciseInfo(): EmptyResult<ExerciseError> {
        val info = client.getCurrentExerciseInfo()

        return when (info.exerciseTrackedStatus) {
            ExerciseTrackedStatus.NO_EXERCISE_IN_PROGRESS -> Result.Success(Unit)
            ExerciseTrackedStatus.OWNED_EXERCISE_IN_PROGRESS -> Result.Error(ExerciseError.ONGOING_OWN_EXERCISE)
            ExerciseTrackedStatus.OTHER_APP_IN_PROGRESS -> Result.Error(ExerciseError.ONGOING_OTHER_EXERCISE)
            else -> Result.Error(ExerciseError.UNKNOWN)
        }
    }

    private fun hasBodySensorsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BODY_SENSORS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
