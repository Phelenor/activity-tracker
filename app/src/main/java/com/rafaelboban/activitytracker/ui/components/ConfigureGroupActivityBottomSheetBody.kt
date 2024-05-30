package com.rafaelboban.activitytracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafaelboban.activitytracker.R
import com.rafaelboban.activitytracker.util.DateHelper
import com.rafaelboban.activitytracker.util.DateHelper.secondsToLocalDate
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.time.Duration

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ConfigureGroupActivityBottomSheetBody(
    onClick: (ActivityType, Long?) -> Unit,
    isCreatingActivity: Boolean,
    modifier: Modifier = Modifier
) {
    val currentYear = remember { LocalDate.now().year }

    var selectedActivityType by remember { mutableStateOf<ActivityType?>(null) }
    var selectedDateTimestamp by remember { mutableStateOf<Long?>(null) }
    var selectedTimeTimestamp by remember { mutableStateOf<Int?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val localDateTimestamp by remember {
        derivedStateOf {
            selectedDateTimestamp?.secondsToLocalDate()?.atStartOfDay(ZoneId.systemDefault())?.toEpochSecond()
        }
    }

    val localDateTimeTimestamp by remember {
        derivedStateOf {
            selectedTimeTimestamp?.let { time ->
                localDateTimestamp?.plus(time.toLong())?.coerceAtLeast(Instant.now().epochSecond)
            } ?: Instant.now().epochSecond
        }
    }

    DatePickerDialog(
        showDialog = showDatePicker,
        initialSelectedTimestamp = selectedDateTimestamp,
        onDismiss = { showDatePicker = false },
        yearRange = currentYear..(currentYear + 1),
        selectableDates = PresentOrFutureSelectableDates,
        onConfirm = { timestamp ->
            selectedTimeTimestamp = 0
            selectedDateTimestamp = timestamp
            showTimePicker = true
        }
    )

    TimePickerDialog(
        showDialog = showTimePicker,
        onDismiss = { showTimePicker = false },
        alwaysEnabled = true,
        onConfirm = { hours, minutes ->
            selectedTimeTimestamp = Duration.parse("${hours}h ${minutes}m").inWholeSeconds.toInt()
        }
    )

    Box(
        modifier = modifier.height(IntrinsicSize.Min)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.select_group_activity_type),
                style = Typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(24.dp))

            FlowRow(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActivityType.entries.forEach { type ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ActivityTypeButton(
                            activityType = type,
                            onClick = { selectedActivityType = it },
                            modifier = Modifier.sizeIn(maxHeight = 88.dp),
                            isSelected = type == selectedActivityType
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = stringResource(id = type.nameRes),
                            style = Typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.set_estimated_start_time),
                style = Typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(24.dp))

            Row {
                val dateText = selectedDateTimestamp?.let { DateHelper.formatTimestampToDate(it) } ?: run { stringResource(R.string.now) }
                val timeText = selectedTimeTimestamp?.let { DateHelper.formatTimestampToTime((localDateTimestamp?.plus(it.toLong()) ?: 0)) } ?: run { stringResource(R.string.now) }

                LabeledItem(
                    label = stringResource(id = R.string.date),
                    value = dateText,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .weight(1f)
                        .background(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showDatePicker = true }
                        .padding(vertical = 6.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                LabeledItem(
                    label = stringResource(id = R.string.time),
                    fontSize = 14.sp,
                    value = timeText,
                    modifier = Modifier
                        .weight(1f)
                        .background(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .applyIf(selectedDateTimestamp != null) { clickable { showTimePicker = true } }
                        .padding(vertical = 6.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.activity_clear_notice),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = Typography.labelLarge
            )

            Spacer(Modifier.height(24.dp))

            ButtonPrimary(
                text = stringResource(R.string.create_group_activity),
                enabled = selectedActivityType != null,
                onClick = { onClick(checkNotNull(selectedActivityType), localDateTimeTimestamp) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        AnimatedVisibility(
            visible = isCreatingActivity,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            LoadingIndicator(modifier = Modifier.fillMaxSize())
        }
    }
}

@Preview(showBackground = true)
@PreviewLightDark
@Composable
private fun ConfigureGroupActivityBottomSheetBodyPreview() {
    ActivityTrackerTheme {
        ConfigureGroupActivityBottomSheetBody(
            onClick = { a, b -> },
            isCreatingActivity = true
        )
    }
}
