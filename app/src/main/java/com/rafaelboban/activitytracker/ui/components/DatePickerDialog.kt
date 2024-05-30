package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rafaelboban.activitytracker.R
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    showDialog: Boolean,
    initialSelectedTimestamp: Long?,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    yearRange: IntRange = DatePickerDefaults.YearRange,
    selectableDates: SelectableDates = DatePickerDefaults.AllDates
) {
    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedTimestamp?.times(1000),
        yearRange = yearRange,
        selectableDates = selectableDates
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

@OptIn(ExperimentalMaterial3Api::class)
object PresentOrFutureSelectableDates : SelectableDates {

    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        return utcTimeMillis >= System.currentTimeMillis()
    }

    override fun isSelectableYear(year: Int): Boolean {
        return year >= LocalDate.now().year
    }
}
