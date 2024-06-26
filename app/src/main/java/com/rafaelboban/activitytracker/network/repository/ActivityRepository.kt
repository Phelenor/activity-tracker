package com.rafaelboban.activitytracker.network.repository

import com.rafaelboban.activitytracker.model.network.Activity
import com.rafaelboban.activitytracker.model.network.CreateGroupActivityRequest
import com.rafaelboban.activitytracker.model.network.JoinGroupActivityRequest
import com.rafaelboban.activitytracker.model.network.LeaveGroupActivityRequest
import com.rafaelboban.activitytracker.network.ApiService
import com.rafaelboban.core.shared.model.ActivityType
import com.skydoves.sandwich.ApiResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepository @Inject constructor(
    private val api: ApiService,
    private val json: Json
) {

    suspend fun saveActivity(activity: Activity, mapSnapshot: ByteArray): ApiResponse<Activity> {
        val activityJson = json.encodeToString(activity)
        val requestBody = activityJson.toRequestBody("application/json".toMediaTypeOrNull())
        val imageRequestBody = mapSnapshot.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, mapSnapshot.size)
        val imagePart = MultipartBody.Part.createFormData("image", "map-snapshot-${activity.startTimestamp}", imageRequestBody)

        return api.postActivity(requestBody, imagePart)
    }

    suspend fun getActivities() = api.getActivities()

    suspend fun getActivity(id: String) = api.getActivity(id)

    suspend fun deleteActivity(id: String) = api.deleteActivity(id)

    suspend fun createGroupActivity(activityType: ActivityType, startTimestamp: Long?) = api.createGroupActivity(CreateGroupActivityRequest(activityType, startTimestamp))

    suspend fun joinGroupActivity(joinCode: String) = api.joinGroupActivity(JoinGroupActivityRequest(joinCode))

    suspend fun getGroupActivity(id: String) = api.getGroupActivity(id)

    suspend fun deleteGroupActivity(id: String) = api.deleteGroupActivity(id)

    suspend fun leaveGroupActivity(id: String) = api.leaveGroupActivity(LeaveGroupActivityRequest(id))

    suspend fun getGroupActivities() = api.getGroupActivities()

    suspend fun getGroupActivityOverview(id: String) = api.getGroupActivityOverview(id)
}
