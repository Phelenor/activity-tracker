package com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rafaelboban.activitytracker.R
import com.rafaelboban.activitytracker.network.model.goals.ActivityGoal
import com.rafaelboban.activitytracker.network.model.goals.ActivityGoalProgress
import com.rafaelboban.activitytracker.network.model.goals.ActivityGoalType
import com.rafaelboban.activitytracker.network.model.goals.GoalValueComparisonType
import com.rafaelboban.activitytracker.ui.components.ButtonSecondary
import com.rafaelboban.activitytracker.ui.screens.activity.ActivityState
import com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.components.ActivityGoalProgressRow
import com.rafaelboban.core.shared.model.ActivityStatus
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography
import kotlinx.collections.immutable.toImmutableList

@Composable
fun ActivityGoalsTab(
    state: ActivityState,
    showAddButton: Boolean,
    showAddGoalDialog: () -> Unit,
    onRemoveGoal: (ActivityGoalType) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        state.goals.forEach { goalProgress ->
            key(goalProgress.goal.type) {
                ActivityGoalProgressRow(
                    goalProgress = goalProgress,
                    activityStatus = state.status,
                    onRemoveClick = onRemoveGoal
                )
            }

            HorizontalDivider(modifier = Modifier.padding(8.dp))
        }

        if (state.goals.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "No goals set for this activity.",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = Typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (showAddButton && state.status == ActivityStatus.NOT_STARTED) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                ButtonSecondary(
                    text = stringResource(R.string.add),
                    icon = Icons.Outlined.Add,
                    onClick = showAddGoalDialog
                )
            }
        }

        if (state.status == ActivityStatus.IN_PROGRESS) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Goal progress updates every 5 seconds.",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = Typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActivityGoalsTabPreview() {
    ActivityTrackerTheme {
        ActivityGoalsTab(
            showAddButton = true,
            showAddGoalDialog = {},
            onRemoveGoal = {},
            state = ActivityState(
                status = ActivityStatus.IN_PROGRESS,
                type = ActivityType.RUN,
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
                }.toImmutableList()
            )
        )
    }
}
