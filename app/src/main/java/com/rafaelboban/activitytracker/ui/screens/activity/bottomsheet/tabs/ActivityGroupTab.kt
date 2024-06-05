package com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.rafaelboban.activitytracker.model.network.GroupActivity
import com.rafaelboban.activitytracker.model.ui.ActivityUserStatus
import com.rafaelboban.activitytracker.network.ws.ActivityMessage
import com.rafaelboban.activitytracker.ui.components.ActivityUserRow
import com.rafaelboban.core.shared.model.ActivityStatus
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun ActivityGroupTab(
    groupActivity: GroupActivity,
    userData: ImmutableList<ActivityMessage.UserDataSnapshot>
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        userData.forEach { user ->
            key(user.userId) {
                ActivityUserRow(
                    activityType = groupActivity.activityType,
                    data = user,
                    status = when {
                        user.duration != null -> ActivityUserStatus.Finished(user.duration)
                        user.userId in groupActivity.activeUsers -> ActivityUserStatus.Active
                        user.userId in groupActivity.connectedUsers -> ActivityUserStatus.Connected
                        else -> ActivityUserStatus.Joined
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActivityWeatherTabPreview() {
    ActivityTrackerTheme {
        ActivityGroupTab(
            groupActivity = GroupActivity(
                id = "id",
                activityType = ActivityType.RUN,
                joinedUsers = emptyList(),
                activeUsers = emptyList(),
                joinCode = "AD2323",
                status = ActivityStatus.IN_PROGRESS,
                userOwnerId = "sdadasd",
                userOwnerName = "Rafael",
                startTimestamp = 31241412,
                connectedUsers = emptyList()
            ),
            userData = List(3) {
                ActivityMessage.UserDataSnapshot(
                    userId = "123",
                    userDisplayName = "Rafael",
                    userImageUrl = null,
                    lat = 1f,
                    long = 1f,
                    distance = 3300,
                    heartRate = 120,
                    speed = 8f
                )
            }.toImmutableList()
        )
    }
}
