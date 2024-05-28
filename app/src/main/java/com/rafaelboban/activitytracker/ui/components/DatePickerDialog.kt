package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rafaelboban.activitytracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    showDialog: Boolean,
    initialSelectedTimestamp: Long?,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    yearRange: IntRange = DatePickerDefaults.YearRange
) {
    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedTimestamp?.times(1000),
        yearRange = yearRange
    )

    if (showDialog) {
        DatePickerDialog(
            modifier = modifier.padding(horizontal = 4.dp),
            onDismissRequest = onDismiss,
            confirmButton = {
                ButtonPrimary(
                    modifier = Modifier.padding(end = 8.dp),
                    text = stringResource(id = R.string.confirm),
                    enabled = dateState.selectedDateMillis != null && ((dateState.selectedDateMillis ?: 0) / 1000) != initialSelectedTimestamp,
                    onClick = {
                        onConfirm(checkNotNull(dateState.selectedDateMillis) / 1000)
                        onDismiss()
                    }
                )
            },
            dismissButton = {
                ButtonPrimary(
                    text = stringResource(id = R.string.cancel),
                    onClick = onDismiss
                )
            }
        ) {
            DatePicker(
                state = dateState,
                showModeToggle = false
            )
        }
    }
}
