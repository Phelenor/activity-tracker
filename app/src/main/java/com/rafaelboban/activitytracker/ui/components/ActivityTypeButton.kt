package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
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
fun ActivityTypeButton(
    activityType: ActivityType,
    onClick: (ActivityType) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(64.dp)
            .aspectRatio(1f)
            .clip(CircleShape)
            .clickable { onClick(activityType) }
            .background(shape = CircleShape, color = MaterialTheme.colorScheme.tertiary)
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = activityType.drawableRes),
            contentDescription = activityType.name,
            tint = MaterialTheme.colorScheme.onTertiary
        )
    }
}

@PreviewLightDark
@Composable
private fun ActivityTypeButtonPreview() {
    ActivityTrackerTheme {
        ActivityTypeButton(
            activityType = ActivityType.RUN,
            onClick = {}
        )
    }
}
