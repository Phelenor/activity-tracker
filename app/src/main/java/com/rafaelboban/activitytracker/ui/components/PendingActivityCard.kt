package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.rafaelboban.activitytracker.model.network.GroupActivity
import com.rafaelboban.activitytracker.util.DateHelper
import com.rafaelboban.core.shared.model.ActivityStatus
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography

@Composable
fun PendingActivityCard(
    groupActivity: GroupActivity,
    navigateToGroupActivity: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primary, width = 2.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable { navigateToGroupActivity(groupActivity.id) }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color = MaterialTheme.colorScheme.tertiary, shape = CircleShape)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.tertiary,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(groupActivity.activityType.drawableRes),
                    tint = MaterialTheme.colorScheme.onTertiary,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = stringResource(groupActivity.activityType.nameRes),
                style = Typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = "Leader: ${groupActivity.userOwnerName}",
                style = Typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        GroupActivityDateSection(
            timestamp = groupActivity.startTimestamp
        )
    }
}

@Composable
private fun GroupActivityDateSection(
    timestamp: Long,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = DateHelper.formatTimestampToDateTime(timestamp),
            style = Typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@PreviewLightDark
@Composable
private fun PendingActivityCardPreview() {
    ActivityTrackerTheme {
        PendingActivityCard(
            navigateToGroupActivity = {},
            groupActivity = GroupActivity(
                id = "id",
                activityType = ActivityType.RUN,
                startedUsers = emptyList(),
                joinedUsers = emptyList(),
                activeUsers = emptyList(),
                joinCode = "AD2323",
                status = ActivityStatus.IN_PROGRESS,
                userOwnerId = "sdadasd",
                userOwnerName = "Rafael",
                startTimestamp = 31241412
            )
        )
    }
}
