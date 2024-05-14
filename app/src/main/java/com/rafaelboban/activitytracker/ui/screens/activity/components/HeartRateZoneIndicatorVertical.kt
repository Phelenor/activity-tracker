package com.rafaelboban.activitytracker.ui.screens.activity.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.rafaelboban.activitytracker.ui.components.applyIf
import com.rafaelboban.core.shared.utils.F
import com.rafaelboban.core.shared.utils.HeartRateZone
import com.rafaelboban.core.shared.utils.color
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme

@Composable
fun HeartRateZoneIndicatorVertical(
    currentZone: HeartRateZone,
    ratioInZone: Float,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val zoneHeightDp = maxHeight / HeartRateZone.Trackable.size.F
        val topPadding = zoneHeightDp * (currentZone.ordinal - 1) + zoneHeightDp * ratioInZone

        val topPaddingAnimated by animateFloatAsState(
            targetValue = if (currentZone == HeartRateZone.AT_REST) 0f else topPadding.value,
            animationSpec = tween(durationMillis = 200),
            label = "zone_indicator_animation"
        )

        Column {
            HeartRateZone.Trackable.forEachIndexed { index, zone ->
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .weight(1f)
                        .applyIf(index == 0) { clip(shape = RoundedCornerShape(topEnd = 8.dp)) }
                        .applyIf(index == HeartRateZone.Trackable.lastIndex) { clip(shape = RoundedCornerShape(bottomEnd = 8.dp)) }
                        .background(color = zone.color)
                )
            }
        }

        Box(
            modifier = Modifier
                .padding(top = (topPaddingAnimated.dp - 3.dp).coerceAtLeast(0.dp))
                .width(14.dp)
                .height(6.dp)
                .background(color = MaterialTheme.colorScheme.error, shape = RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp))
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HeartRateZoneIndicatorVerticalPreview() {
    ActivityTrackerTheme {
        HeartRateZoneIndicatorVertical(
            currentZone = HeartRateZone.ANAEROBIC,
            ratioInZone = 0.2f
        )
    }
}
