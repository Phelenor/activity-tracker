package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafaelboban.activitytracker.R
import com.rafaelboban.activitytracker.network.model.goals.ActivityGoalType
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Duration.Companion.hours

@Composable
fun AddActivityGoalDialog(
    goalTypes: ImmutableList<ActivityGoalType>,
    onActionClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedGoal by remember { mutableStateOf(goalTypes.first()) }
    var selectedGoalValue by remember { mutableStateOf<Float?>(null) }

    Column(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .background(shape = RoundedCornerShape(32.dp), color = MaterialTheme.colorScheme.surfaceBright)
            .padding(vertical = 24.dp)
    ) {
        Text(
            text = "Add goal",
            style = Typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalDivider()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 142.dp)
                .verticalScroll(rememberScrollState())
        ) {
            goalTypes.forEach { goal ->
                Column(
                    modifier = Modifier
                        .applyIf(selectedGoal == goal) { background(color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)) }
                        .clickable { selectedGoal = goal }
                ) {
                    Text(
                        text = stringResource(goal.stringRes),
                        style = Typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .padding(horizontal = 24.dp)
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                }
            }
        }

        HorizontalDivider()

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedGoal) {
            ActivityGoalType.DISTANCE -> NumberPicker(
                label = stringResource(R.string.distance) + " km",
                onNumber = { selectedGoalValue = it },
                isValid = { input ->
                    input.isBlank() || input.toFloatOrNull()?.let { it > 0 && it < 150 } ?: false
                }
            )

            ActivityGoalType.DURATION -> DurationPicker()
            ActivityGoalType.CALORIES -> NumberPicker(
                label = stringResource(R.string.calories) + " kcal",
                onNumber = { selectedGoalValue = it },
                isValid = { input ->
                    input.isBlank() || input.toFloatOrNull()?.let { it > 0 && it < 1500 } ?: false
                }
            )

            ActivityGoalType.AVG_HEART_RATE -> Box {}
            ActivityGoalType.AVG_SPEED -> Box {}
            ActivityGoalType.IN_HR_ZONE -> Box {}
            ActivityGoalType.BELOW_ABOVE_HR_ZONE -> Box {}
        }

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(8.dp))

        DialogButtonRow(
            modifier = Modifier.padding(horizontal = 24.dp),
            positiveText = "Add",
            onCancelClick = {
                onDismissClick()
            },
            onActionClick = {
                onActionClick()
                onDismissClick()
            }
        )
    }
}

@Composable
fun DurationPicker(
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var hours by remember { mutableIntStateOf(0) }
    var minutes by remember { mutableIntStateOf(0) }

    TimePickerDialog(
        showDialog = showDialog,
        onDismiss = { showDialog = false },
        onConfirm = { newHours, newMinutes ->
            hours = newHours
            minutes = newMinutes
        }
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceContainerHighest, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .clickable { showDialog = true }
                .padding(16.dp)
        ) {
            Text(
                text = "%02d".format(hours),
                style = Typography.displayLarge,
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = ":",
                style = Typography.displaySmall,
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(2.dp)
            )

            Text(
                text = "%02d".format(minutes),
                style = Typography.displayLarge,
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun NumberPicker(
    label: String,
    onNumber: (Float) -> Unit,
    isValid: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp, horizontal = 24.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = {
                if (it.toIntOrNull() != null && isValid(it)) {
                    text = it
                    onNumber(checkNotNull(it.toFloatOrNull()))
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = Typography.bodyLarge,
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            isError = !isValid(text),
            modifier = modifier.fillMaxWidth(),
            colors = trackerOutlinedTextFieldColors(),
            label = {
                Text(
                    text = label,
                    style = Typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )
    }
}

@Preview
@Composable
private fun AddActivityGoalsDialogPreview() {
    ActivityTrackerTheme {
        AddActivityGoalDialog(
            onActionClick = {},
            onDismissClick = {},
            goalTypes = ActivityGoalType.entries.toImmutableList()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NumberPickerPreview() {
    ActivityTrackerTheme {
        NumberPicker(
            label = "Calories kcal",
            onNumber = {},
            isValid = { true }
        )
    }
}

@Preview
@Composable
private fun DurationPickerPreview() {
    ActivityTrackerTheme {
        DurationPicker()
    }
}
