package com.rafaelboban.activitytracker.wear.tracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServices
import androidx.health.services.client.HealthServicesException
import androidx.health.services.client.clearUpdateCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.BatchingMode
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
import com.rafaelboban.core.shared.model.HeartRatePoint
import com.rafaelboban.core.shared.utils.EmptyResult
import com.rafaelboban.core.shared.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import kotlin.math.roundToInt
import kotlin.time.toKotlinDuration

class HealthServicesExerciseTracker(private val context: Context) {

    private val client = HealthServices.getClient(context).exerciseClient

    private var exerciseType = ExerciseType.RUNNING

    val healthData: Flow<HealthData>
        get() = callbackFlow {
            val callback = object : ExerciseUpdateCallback {

                override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
                    val heartRates = update.latestMetrics.getData(DataType.HEART_RATE_BPM)
                    val calories = update.latestMetrics.getData(DataType.CALORIES_TOTAL)

                    val totalCaloriesBurned = calories?.total?.roundToInt()

                    heartRates.forEachIndexed { index, heartRate ->
                        trySend(
                            HealthData(
                                calories = if (index == 0) totalCaloriesBurned else null,
                                heartRate = HeartRatePoint(
                                    heartRate = heartRate.value.roundToInt(),
                                    timestamp = heartRate.timeDurationFromBoot.toKotlinDuration()
                                )
                            )
                        )
                    }

                    if (heartRates.isEmpty()) {
                        trySend(
                            HealthData(
                                calories = totalCaloriesBurned,
                                heartRate = null
                            )
                        )
                    }
                }

                override fun onAvailabilityChanged(dataType: DataType<*, *>, availability: Availability) {
                    Timber.tag("HEALTH_SERVICES").i("Health Services availability = $availability")
                }

                override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) = Unit

                override fun onRegistered() {
                    Timber.tag("HEALTH_SERVICES").i("Health Services registered.")
                }

                override fun onRegistrationFailed(throwable: Throwable) {
                    Timber.tag("HEALTH_SERVICES").e(throwable)
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

    suspend fun isCalorieTrackingSupported(): Boolean {
        return hasActivityRecognitionPermission() && runCatching {
            val capabilities = client.getCapabilities()
            val supportedDataTypes = capabilities
                .typeToCapabilities[exerciseType]
                ?.supportedDataTypes ?: setOf()

            DataType.CALORIES_TOTAL in supportedDataTypes
        }.getOrDefault(false)
    }

    private suspend fun isBatchingModeSupported(): Boolean {
        return hasBodySensorsBackgroundPermission() && runCatching {
            val capabilities = client.getCapabilities()
            val supportedBatchingModes = capabilities.supportedBatchingModeOverrides

            BatchingMode.HEART_RATE_5_SECONDS in supportedBatchingModes
        }.getOrDefault(false)
    }

    suspend fun prepareExercise(exerciseType: ExerciseType): EmptyResult<ExerciseError> {
        this.exerciseType = exerciseType

        if (isHeartRateTrackingSupported().not()) {
            return Result.Error(ExerciseError.TRACKING_NOT_SUPPORTED)
        }

        val result = getActiveExerciseInfo()
        if (result is Result.Error) {
            return result
        }

        val config = WarmUpConfig(
            exerciseType = exerciseType,
            dataTypes = setOfNotNull(DataType.HEART_RATE_BPM)
        )

        client.prepareExercise(config)

        return Result.Success(Unit)
    }

    suspend fun startExercise(exerciseType: ExerciseType): EmptyResult<ExerciseError> {
        this.exerciseType = exerciseType

        if (isHeartRateTrackingSupported().not()) {
            return Result.Error(ExerciseError.TRACKING_NOT_SUPPORTED)
        }

        val result = getActiveExerciseInfo()
        if (result is Result.Error) {
            return result
        }

        val dataTypes = setOfNotNull(
            DataType.HEART_RATE_BPM,
            DataType.CALORIES_TOTAL.takeIf { isCalorieTrackingSupported() }
        )

        val configBuilder = ExerciseConfig.builder(exerciseType)
            .setDataTypes(dataTypes)
            .setIsAutoPauseAndResumeEnabled(false)

        if (isBatchingModeSupported()) {
            configBuilder.setBatchingModeOverrides(setOf(BatchingMode.HEART_RATE_5_SECONDS))
        }

        client.startExercise(configBuilder.build())
        Timber.tag("HEALTH_SERVICES").i("Exercise Started - $exerciseType")

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
            Timber.tag("HEALTH_SERVICES").i("Exercise Resumed")
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
            Timber.tag("HEALTH_SERVICES").i("Exercise Paused")
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
            Timber.tag("HEALTH_SERVICES").i("Exercise Stopped")
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

    private fun hasBodySensorsBackgroundPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BODY_SENSORS_BACKGROUND
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasActivityRecognitionPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
