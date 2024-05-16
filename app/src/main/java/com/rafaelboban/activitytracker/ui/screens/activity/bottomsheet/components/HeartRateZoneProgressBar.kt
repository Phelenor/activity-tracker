package com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rafaelboban.core.shared.utils.HeartRateZone
import com.rafaelboban.core.shared.utils.color
import com.rafaelboban.core.shared.utils.labelShort
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography
import kotlin.math.roundToInt

@Composable
fun HeartRateZoneProgressBar(
    zone: HeartRateZone,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec)

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp)
        ) {
            Text(
                style = Typography.displaySmall,
                color = zone.color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
                text = buildAnnotatedString {
                    append(zone.ordinal.toString())
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Normal)) {
                        append(" (${zone.labelShort})")
                    }
                }
            )

            Spacer(modifier = Modifier.width(4.dp))

            Row(
                modifier = Modifier.weight(2f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.widthIn(min = 44.dp),
                    text = "%d%%".format((animatedProgress * 100).roundToInt()),
                    textAlign = TextAlign.End
                )

                Spacer(modifier = Modifier.width(8.dp))

                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier.weight(1f),
                    color = zone.color,
                    backgroundColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun HeartRateZoneProgressBarPreview() {
    ActivityTrackerTheme {
        HeartRateZoneProgressBar(
            zone = HeartRateZone.ANAEROBIC,
            progress = 0.6f
        )
    }
}
