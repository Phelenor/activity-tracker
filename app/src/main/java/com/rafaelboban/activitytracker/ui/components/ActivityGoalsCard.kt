package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rafaelboban.activitytracker.R
import com.rafaelboban.activitytracker.network.model.goals.ActivityGoal
import com.rafaelboban.activitytracker.network.model.goals.ActivityGoalProgress
import com.rafaelboban.activitytracker.network.model.goals.ActivityGoalType
import com.rafaelboban.activitytracker.network.model.goals.GoalValueComparisonType
import com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.components.ActivityGoalProgressRow
import com.rafaelboban.core.shared.model.ActivityStatus
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography

@Composable
fun ActivityGoalsCard(
    goals: List<ActivityGoalProgress>,
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
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.goals),
                style = Typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )

            Text(
                text = "${goals.count { it.isAchieved }}/${goals.size}",
                style = Typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        goals.forEach { goalProgress ->
            ActivityGoalProgressRow(
                goalProgress = goalProgress,
                activityStatus = ActivityStatus.FINISHED,
                onRemoveClick = {}
            )

            HorizontalDivider(modifier = Modifier.padding(8.dp))
        }
    }
}

@Preview
@Composable
private fun ActivityGoalsCardPreview() {
    ActivityTrackerTheme {
        ActivityGoalsCard(
            goals = List(5) {
                ActivityGoalProgress(
                    currentValue = 1.2f,
                    goal = ActivityGoal(
                        type = ActivityGoalType.DISTANCE,
                        valueType = GoalValueComparisonType.GREATER,
                        value = 3.4f,
                        label = "distance"
                    )
                )
            }
        )
    }
}
