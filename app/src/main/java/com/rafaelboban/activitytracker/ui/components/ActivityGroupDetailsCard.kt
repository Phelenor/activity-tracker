package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rafaelboban.activitytracker.R
import com.rafaelboban.activitytracker.model.User
import com.rafaelboban.activitytracker.model.network.GroupActivityOverview
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography
import java.time.Instant
import kotlin.time.Duration.Companion.days

@Composable
fun ActivityGroupDetailsCard(
    groupActivityOverview: GroupActivityOverview,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primary, width = 2.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.group_participants),
            style = Typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )

        groupActivityOverview.users.forEach { user ->
            ActivityOverviewUserRow(
                user = user,
                isOwner = groupActivityOverview.ownerId == user.id
            )
        }
    }
}

@Preview
@Composable
private fun ActivityGroupDetailsCardPreview() {
    ActivityTrackerTheme {
        ActivityGroupDetailsCard(
            groupActivityOverview = GroupActivityOverview(
                ownerId = "owner",
                users = listOf(
                    User(
                        id = "owner",
                        email = "test@gmail.com",
                        imageUrl = "https://lh3.googleusercontent.com/a/ACg8ocIkI-iHUZ-RnNOU6tqTO7NPPLQ_pZbVZLV-Ha6Lx8rV6aPk_uc=s96-c",
                        name = "Johnny Silverhand",
                        displayName = "Johnny Silverhand",
                        weight = 83,
                        height = 192,
                        birthTimestamp = Instant.now().epochSecond - (365.days.inWholeSeconds * 24)
                    ),
                    User(
                        id = "213141",
                        email = "test@gmail.com",
                        imageUrl = "https://lh3.googleusercontent.com/a/ACg8ocIkI-iHUZ-RnNOU6tqTO7NPPLQ_pZbVZLV-Ha6Lx8rV6aPk_uc=s96-c",
                        name = "Johnny Silverhand",
                        displayName = "Johnny Silverhand",
                        weight = 83,
                        height = 192,
                        birthTimestamp = Instant.now().epochSecond - (365.days.inWholeSeconds * 24)
                    ),
                    User(
                        id = "213141",
                        email = "test@gmail.com",
                        imageUrl = "https://lh3.googleusercontent.com/a/ACg8ocIkI-iHUZ-RnNOU6tqTO7NPPLQ_pZbVZLV-Ha6Lx8rV6aPk_uc=s96-c",
                        name = "Johnny Silverhand",
                        displayName = "Johnny Silverhand",
                        weight = 83,
                        height = 192,
                        birthTimestamp = Instant.now().epochSecond - (365.days.inWholeSeconds * 24)
                    )
                )
            )
        )
    }
}
