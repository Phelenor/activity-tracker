package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.rafaelboban.activitytracker.model.ActivityType
import com.rafaelboban.activitytracker.ui.theme.ActivityTrackerTheme

@Composable
fun ActivityTypeIcon(
    activityType: ActivityType,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .background(shape = CircleShape, color = MaterialTheme.colorScheme.tertiary)
            .padding(4.dp)
    ) {
        Icon(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = activityType.drawableRes),
            contentDescription = activityType.name,
            tint = MaterialTheme.colorScheme.onTertiary
        )
    }
}

@PreviewLightDark
@Composable
private fun ActivityTypeIconPreview() {
    ActivityTrackerTheme {
        ActivityTypeIcon(
            modifier = Modifier.size(24.dp),
            activityType = ActivityType.RUN
        )
    }
}
