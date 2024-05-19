package com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rafaelboban.activitytracker.network.model.goals.ActivityGoal
import com.rafaelboban.activitytracker.network.model.goals.ActivityGoalProgress
import com.rafaelboban.activitytracker.network.model.goals.ActivityGoalType
import com.rafaelboban.activitytracker.network.model.goals.GoalValueComparisonType
import com.rafaelboban.core.shared.model.ActivityStatus
import com.rafaelboban.core.shared.utils.ActivityDataFormatter.formatElapsedTimeDisplay
import com.rafaelboban.core.shared.utils.ActivityDataFormatter.roundToDecimals
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography
import java.time.Duration
import kotlin.math.roundToInt
import kotlin.time.toKotlinDuration

@Composable
fun ActivityGoalProgressRow(
    goalProgress: ActivityGoalProgress,
    activityStatus: ActivityStatus,
    onRemoveClick: (ActivityGoalType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 16.dp)
    ) {
        Text(
            text = stringResource(goalProgress.goal.type.stringRes),
            style = Typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(8.dp))

        Text(
            style = Typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            text = getValueText(goalProgress.goal.type, goalProgress.goal.value, goalProgress.currentValue, goalProgress.goal.valueType, label = goalProgress.goal.label)
        )

        Spacer(modifier = Modifier.width(8.dp))

        if (activityStatus != ActivityStatus.NOT_STARTED) {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = if (goalProgress.isAchieved) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (goalProgress.isAchieved) Color(0xFF0da63b) else MaterialTheme.colorScheme.error
            )
        } else {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .size(16.dp)
                    .background(shape = CircleShape, color = MaterialTheme.colorScheme.errorContainer)
                    .clip(CircleShape)
                    .clickable { onRemoveClick(goalProgress.goal.type) }
                    .padding(2.dp)
            )
        }
    }
}

private fun getValueText(
    type: ActivityGoalType,
    value: Float,
    currentValue: Float,
    comparisonType: GoalValueComparisonType,
    label: String?
): String {
    return when (type) {
        ActivityGoalType.DISTANCE -> "${currentValue.roundToDecimals(1)}/${value.roundToDecimals(1)} km"
        ActivityGoalType.CALORIES -> "${currentValue.roundToInt()}/${value.roundToInt()} kcal"
        ActivityGoalType.AVG_HEART_RATE -> {
            "${currentValue.roundToInt()} (${comparisonType.label} ${value.roundToInt()} bpm)"
        }

        ActivityGoalType.AVG_SPEED -> {
            "${currentValue.roundToDecimals(1)} (G: ${comparisonType.label} ${value.roundToDecimals(1)} km/h)"
        }

        ActivityGoalType.AVG_PACE -> {
            "${currentValue.roundToDecimals(1)} (G: ${comparisonType.label} ${value.roundToDecimals(1)} min/km)"
        }

        ActivityGoalType.IN_HR_ZONE -> {
            val currentPct = "${currentValue.roundToInt()}%"
            val goalPct = "${value.roundToInt()}%"
            "In Zone $label: $currentPct (G: ${comparisonType.label} $goalPct)"
        }

        ActivityGoalType.BELOW_HR_ZONE -> {
            val currentPct = "${currentValue.roundToInt()}%"
            val goalPct = "${value.roundToInt()}%"
            "Below Zone $label: $currentPct (G: $goalPct)"
        }

        ActivityGoalType.ABOVE_HR_ZONE -> {
            val currentPct = "${currentValue.roundToInt()}%"
            val goalPct = "${value.roundToInt()}%"
            "Above Zone $label: $currentPct (G: $goalPct)"
        }

        ActivityGoalType.DURATION -> {
            val durationGoal = Duration.ofSeconds(value.roundToInt().toLong()).toKotlinDuration().formatElapsedTimeDisplay()
            val durationCurrent = Duration.ofSeconds(currentValue.roundToInt().toLong()).toKotlinDuration().formatElapsedTimeDisplay()
            "$durationCurrent/$durationGoal"
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ActivityGoalRowPreview() {
    ActivityTrackerTheme {
        ActivityGoalProgressRow(
            activityStatus = ActivityStatus.IN_PROGRESS,
            onRemoveClick = {},
            goalProgress = ActivityGoalProgress(
                currentValue = 0.2f,
                goal = ActivityGoal(
                    type = ActivityGoalType.IN_HR_ZONE,
                    valueType = GoalValueComparisonType.GREATER,
                    value = 0.3f,
                    label = "4"
                )
            )
        )
    }
}
