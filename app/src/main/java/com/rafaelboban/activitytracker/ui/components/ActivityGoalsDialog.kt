package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafaelboban.activitytracker.R
import com.rafaelboban.activitytracker.network.model.goals.ActivityGoal
import com.rafaelboban.activitytracker.network.model.goals.ActivityGoalType
import com.rafaelboban.activitytracker.network.model.goals.GoalValueComparisonType
import com.rafaelboban.core.shared.utils.HeartRateZone
import com.rafaelboban.core.shared.utils.color
import com.rafaelboban.core.shared.utils.index
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Duration

/**
 *
 * A mess, made to work
 *
 */

@Composable
fun AddActivityGoalDialog(
    goalTypes: ImmutableList<ActivityGoalType>,
    onAddClick: (ActivityGoal) -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedGoal by remember { mutableStateOf(goalTypes.first()) }
    var selectedGoalValue by remember { mutableStateOf<Float?>(null) }
    var selectedGoalComparisonType by remember { mutableStateOf(GoalValueComparisonType.GREATER) }
    var goalLabel by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedGoal) {
        selectedGoalValue = null
        selectedGoalComparisonType = GoalValueComparisonType.GREATER
        goalLabel = null
    }

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
                .heightIn(max = 160.dp)
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
            ActivityGoalType.DISTANCE -> {
                NumberPicker(
                    label = stringResource(R.string.distance) + " km",
                    onNumber = { selectedGoalValue = it },
                    isValid = { input ->
                        input.isBlank() || input.toFloatOrNull()?.let { it > 0 && it < 150 } ?: false
                    }
                )
            }

            ActivityGoalType.DURATION -> {
                DurationPicker(
                    onConfirm = { selectedGoalValue = it.toFloat() }
                )
            }

            ActivityGoalType.CALORIES -> {
                NumberPicker(
                    label = stringResource(R.string.calories) + " kcal",
                    onNumber = { selectedGoalValue = it },
                    isValid = { input ->
                        input.isBlank() || input.toFloatOrNull()?.let { it > 0 && it < 1500 } ?: false
                    }
                )
            }

            ActivityGoalType.AVG_HEART_RATE -> {
                NumberPickerWithComparisonType(
                    label = stringResource(R.string.avg_heartrate) + " bpm",
                    onConfirm = { value, type ->
                        selectedGoalValue = value
                        selectedGoalComparisonType = type
                    },
                    isValid = { input ->
                        input.isBlank() || input.toFloatOrNull()?.let { it > 40 && it < 250 } ?: false
                    }
                )
            }

            ActivityGoalType.AVG_SPEED -> {
                NumberPickerWithComparisonType(
                    label = stringResource(R.string.avg_speed) + " km/h",
                    onConfirm = { value, type ->
                        selectedGoalValue = value
                        selectedGoalComparisonType = type
                    },
                    isValid = { input ->
                        input.isBlank() || input.toFloatOrNull()?.let { it > 0 && it < 50 } ?: false
                    }
                )
            }

            ActivityGoalType.AVG_PACE -> {
                NumberPickerWithComparisonType(
                    label = stringResource(R.string.avg_pace) + " min/km",
                    onConfirm = { value, type ->
                        selectedGoalValue = value
                        selectedGoalComparisonType = type
                    },
                    isValid = { input ->
                        input.isBlank() || input.toFloatOrNull()?.let { it in 1.0..25.0 } ?: false
                    }
                )
            }

            ActivityGoalType.IN_HR_ZONE -> {
                NumberPickerWithZones(
                    label = "% in HR Zone",
                    onZoneChanged = { zoneIndex ->
                        goalLabel = zoneIndex
                    },
                    onInput = { value, comparisonType ->
                        selectedGoalValue = value
                        selectedGoalComparisonType = comparisonType
                    }
                )
            }

            ActivityGoalType.BELOW_ABOVE_HR_ZONE -> {
                NumberPickerWithZones(
                    label = "% above/below HR Zone (incl.)",
                    onZoneChanged = { zoneIndex ->
                        goalLabel = zoneIndex
                    },
                    onInput = { value, comparisonType ->
                        selectedGoalValue = value
                        selectedGoalComparisonType = comparisonType
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(8.dp))

        DialogButtonRow(
            modifier = Modifier.padding(horizontal = 24.dp),
            actionEnabled = selectedGoalValue != null,
            positiveText = "Add",
            onCancelClick = {
                onDismissClick()
            },
            onActionClick = {
                val goal = ActivityGoal(
                    type = selectedGoal,
                    value = checkNotNull(selectedGoalValue),
                    valueType = selectedGoalComparisonType,
                    label = goalLabel
                )

                onAddClick(goal)
                onDismissClick()
            }
        )
    }
}

@Composable
fun DurationPicker(
    onConfirm: (Long) -> Unit,
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
            onConfirm(Duration.parse("${hours}h ${minutes}m").inWholeSeconds)
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
                if (it.toFloatOrNull() != null || it.isBlank()) {
                    text = it
                    if (isValid(it)) {
                        onNumber(checkNotNull(it.toFloatOrNull()))
                    }
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

@Composable
private fun NumberPickerWithComparisonType(
    label: String,
    onConfirm: (Float, GoalValueComparisonType) -> Unit,
    isValid: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(GoalValueComparisonType.GREATER) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .height(IntrinsicSize.Max)
            .padding(vertical = 16.dp, horizontal = 24.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = {
                if (it.toFloatOrNull() != null || it.isBlank()) {
                    text = it
                    if (isValid(it)) {
                        onConfirm(checkNotNull(it.toFloatOrNull()), selectedType)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = Typography.bodyLarge,
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            isError = !isValid(text),
            modifier = modifier.weight(1f),
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

        Spacer(Modifier.width(8.dp))

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(IntrinsicSize.Max)
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(4.dp))
                .border(width = 1.dp, shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primary)

        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .applyIf(selectedType == GoalValueComparisonType.GREATER) { background(color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)) }
                    .clickable {
                        selectedType = GoalValueComparisonType.GREATER
                        text
                            .toFloatOrNull()
                            ?.let { onConfirm(it, selectedType) }
                    }
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = GoalValueComparisonType.GREATER.label,
                    style = Typography.displaySmall
                )
            }
            HorizontalDivider()
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .applyIf(selectedType == GoalValueComparisonType.LESS) { background(color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)) }
                    .clickable {
                        selectedType = GoalValueComparisonType.LESS
                        text
                            .toFloatOrNull()
                            ?.let { onConfirm(it, selectedType) }
                    }
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = GoalValueComparisonType.LESS.label,
                    style = Typography.displaySmall
                )
            }
        }
    }
}

@Composable
fun NumberPickerWithZones(
    label: String,
    onInput: (Float, GoalValueComparisonType) -> Unit,
    onZoneChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedZoneIndexLabel by remember { mutableStateOf("1") }

    Column(modifier = Modifier.fillMaxWidth()) {
        NumberPickerWithComparisonType(
            label = label,
            onConfirm = { value, comparisonType ->
                onZoneChanged(selectedZoneIndexLabel)
                onInput(value, comparisonType)
            },
            isValid = { string ->
                string.isBlank() || string.toIntOrNull() in 0..250
            }
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 8.dp)
        ) {
            HeartRateZone.Trackable.forEach { zone ->
                Text(
                    text = zone.index.toString(),
                    textAlign = TextAlign.Center,
                    style = Typography.displayLarge,
                    fontSize = 32.sp,
                    color = zone.color,
                    modifier = Modifier
                        .weight(1f)
                        .background(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh)
                        .applyIf(zone.index.toString() == selectedZoneIndexLabel) { border(width = 2.dp, color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp)) }
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            selectedZoneIndexLabel = zone.index.toString()
                            onZoneChanged(zone.index.toString())
                        }
                )
            }
        }
    }
}

@Preview
@Composable
private fun AddActivityGoalsDialogPreview() {
    ActivityTrackerTheme {
        AddActivityGoalDialog(
            onAddClick = {},
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

@Preview(showBackground = true)
@Composable
private fun NumberPickerWithComparisonTypePreview() {
    ActivityTrackerTheme {
        NumberPickerWithComparisonType(
            label = "Calories kcal",
            onConfirm = { a, b -> },
            isValid = { true }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DurationPickerPreview() {
    ActivityTrackerTheme {
        DurationPicker(
            onConfirm = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NumberPickerWithZonesPreview() {
    ActivityTrackerTheme {
        NumberPickerWithZones(
            label = "% in Zone",
            onInput = { a, b -> },
            onZoneChanged = {}
        )
    }
}
