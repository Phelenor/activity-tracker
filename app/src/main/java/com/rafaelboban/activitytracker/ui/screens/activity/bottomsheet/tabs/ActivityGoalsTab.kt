package com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rafaelboban.activitytracker.network.model.goals.ActivityGoal
import com.rafaelboban.activitytracker.network.model.goals.ActivityGoalProgress
import com.rafaelboban.activitytracker.network.model.goals.ActivityGoalType
import com.rafaelboban.activitytracker.network.model.goals.GoalValueComparisonType
import com.rafaelboban.activitytracker.ui.components.ButtonSecondary
import com.rafaelboban.activitytracker.ui.screens.activity.ActivityState
import com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet.components.ActivityGoalProgressRow
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import kotlinx.collections.immutable.toImmutableList

@Composable
fun ActivityGoalsTab(
    state: ActivityState,
    showAddGoalDialog: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        state.goals.forEach { goalProgress ->
            ActivityGoalProgressRow(
                goalProgress = goalProgress,
                activityStatus = state.status,
                activityType = state.type,
                onRemoveClick = {}
            )

            HorizontalDivider(modifier = Modifier.padding(8.dp))
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            ButtonSecondary(
                text = "Add",
                icon = Icons.Outlined.Add,
                onClick = showAddGoalDialog
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActivityGoalsTabPreview() {
    ActivityTrackerTheme {
        ActivityGoalsTab(
            showAddGoalDialog = {},
            state = ActivityState(
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
