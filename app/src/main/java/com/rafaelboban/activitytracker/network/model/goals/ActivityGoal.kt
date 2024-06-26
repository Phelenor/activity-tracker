package com.rafaelboban.activitytracker.network.model.goals

import kotlinx.serialization.Serializable

@Serializable
data class ActivityGoal(
    val type: ActivityGoalType,
    val valueType: GoalValueComparisonType,
    val label: String?,
    val value: Float
)

enum class GoalValueComparisonType(val label: String) {
    GREATER(">"),
    LESS("<")
}

@Serializable
data class ActivityGoalProgress(
    val goal: ActivityGoal,
    val currentValue: Float
) {

    val isAchieved: Boolean
        get() = when (goal.valueType) {
            GoalValueComparisonType.GREATER -> currentValue >= goal.value
            GoalValueComparisonType.LESS -> currentValue <= goal.value
        }
}
