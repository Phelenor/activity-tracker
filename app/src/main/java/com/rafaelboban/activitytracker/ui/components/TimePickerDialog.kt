package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rafaelboban.activitytracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    showDialog: Boolean,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    initialHour: Int = 0,
    initialMinute: Int = 0
) {
    val state = rememberTimePickerState(
        is24Hour = true,
        initialHour = initialHour,
        initialMinute = initialMinute
    )

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                ButtonPrimary(
                    modifier = Modifier.padding(end = 8.dp),
                    text = stringResource(id = R.string.confirm),
                    enabled = state.hour != 0 || state.minute != 0,
                    onClick = {
                        onConfirm(state.hour, state.minute)
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
            TimePicker(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                state = state
            )
        }
    }
}
