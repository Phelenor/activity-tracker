package com.rafaelboban.activitytracker.ui.screens.activity.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rafaelboban.activitytracker.model.ActivityType
import com.rafaelboban.activitytracker.ui.components.ActivityTypeIcon
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography

@Composable
fun ActivityTopAppBar(
    activityType: ActivityType,
    gpsOk: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gpsBlinkAnimation by rememberInfiniteTransition(label = "gps_blink").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        label = "gps_blink",
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 8.dp)
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "back",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        Text(
            text = "Now Running",
            style = Typography.displayMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        ActivityTypeIcon(
            modifier = Modifier.size(32.dp),
            activityType = activityType
        )

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            imageVector = if (gpsOk) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
            contentDescription = null,
            tint = if (gpsOk) Color(0xFF0da63b) else MaterialTheme.colorScheme.error,
            modifier = Modifier
                .size(18.dp)
                .alpha(gpsBlinkAnimation)
        )

        Spacer(modifier = Modifier.width(4.dp))
    }
}

@Preview
@Composable
private fun ActivityTopAppBarPreview() {
    ActivityTrackerTheme {
        ActivityTopAppBar(
            gpsOk = true,
            activityType = ActivityType.CYCLING,
            onBackClick = {}
        )
    }
}
