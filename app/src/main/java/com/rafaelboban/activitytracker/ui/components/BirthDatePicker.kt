package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
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
fun BirthDatePicker(
    showDialog: Boolean,
    initialSelectedTimestamp: Long?,
    modifier: Modifier = Modifier,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedTimestamp?.times(1000),
        yearRange = 1900..2012
    )

    if (showDialog) {
        DatePickerDialog(
            modifier = Modifier.padding(horizontal = 4.dp),
            onDismissRequest = onDismiss,
            confirmButton = {
                ButtonPrimary(
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
