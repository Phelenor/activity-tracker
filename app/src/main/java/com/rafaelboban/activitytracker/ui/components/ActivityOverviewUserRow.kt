package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rafaelboban.activitytracker.model.User
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography
import java.time.Instant
import kotlin.time.Duration.Companion.days

@Composable
fun ActivityOverviewUserRow(
    user: User,
    isOwner: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                UserImage(
                    modifier = Modifier.size(40.dp),
                    imageUrl = user.imageUrl
                )

                Spacer(Modifier.width(16.dp))

                Text(
                    text = user.displayName,
                    style = Typography.displayMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }

            if (isOwner) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = "leader",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        HorizontalDivider()
    }
}

@Preview(showBackground = true)
@Composable
private fun ActivityOverviewUserRowPreview() {
    ActivityTrackerTheme {
        ActivityOverviewUserRow(
            isOwner = false,
            user = User(
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
    }
}
