package com.rafaelboban.activitytracker.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

inline fun <reified T : ListenableWorker> buildOneTimeWorkRequest(
    context: Context,
    workPolicy: ExistingWorkPolicy = ExistingWorkPolicy.REPLACE
) {
    val builder = OneTimeWorkRequestBuilder<T>()
        .setRetryBackoffPolicy()
        .requireNetwork()

    WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
        T::class.java.simpleName,
        workPolicy,
        builder.build()
    )
}

fun OneTimeWorkRequest.Builder.setRetryBackoffPolicy(): OneTimeWorkRequest.Builder {
    return setBackoffCriteria(
        BackoffPolicy.EXPONENTIAL,
        5,
        TimeUnit.SECONDS
    )
}

fun OneTimeWorkRequest.Builder.requireNetwork(): OneTimeWorkRequest.Builder {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    return setConstraints(constraints)
}
