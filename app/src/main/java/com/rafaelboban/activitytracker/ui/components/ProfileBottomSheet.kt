package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.rafaelboban.activitytracker.R
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography

@Composable
fun BottomSheetButtonRow(
    actionText: String,
    onActionClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionEnabled: Boolean = true,
    actionButtonColor: Color = MaterialTheme.colorScheme.primaryContainer,
    actionButtonTextColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        OutlinedButtonPrimary(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.cancel),
            onClick = onCancelClick
        )

        Spacer(modifier = Modifier.width(8.dp))

        ButtonPrimary(
            modifier = Modifier.weight(1f),
            text = actionText,
            containerColor = actionButtonColor,
            textColor = actionButtonTextColor,
            enabled = actionEnabled,
            onClick = {
                onActionClick()
                onCancelClick()
            }
        )
    }
}

@Composable
fun ConfirmActionBottomSheet(
    title: String,
    actionText: String,
    onActionClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionButtonColor: Color = MaterialTheme.colorScheme.primary,
    actionButtonTextColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp)
    ) {
        Text(
            text = title,
            style = Typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        subtitle?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = subtitle,
                style = Typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        BottomSheetButtonRow(
            actionText = actionText,
            actionButtonColor = actionButtonColor,
            actionButtonTextColor = actionButtonTextColor,
            onCancelClick = onDismissClick,
            onActionClick = {
                onActionClick()
                onDismissClick()
            }
        )
    }
}

@Composable
fun ChangeNameBottomSheet(
    currentName: String,
    onActionClick: (String) -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(currentName) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp)
    ) {
        Text(
            text = stringResource(id = R.string.update_name),
            style = Typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            textStyle = Typography.bodyLarge,
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            isError = name.trim().length !in 3 until 30,
            modifier = modifier.fillMaxWidth(),
            colors = trackerOutlinedTextFieldColors(),
            supportingText = {
                Text(
                    text = stringResource(id = R.string.name_validation),
                    style = Typography.labelMedium,
                    color = MaterialTheme.colorScheme.inverseSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            },
            label = {
                Text(
                    text = stringResource(id = R.string.name),
                    style = Typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )

        Spacer(modifier = Modifier.height(36.dp))

        BottomSheetButtonRow(
            actionText = stringResource(id = R.string.confirm),
            actionEnabled = name.length in 3 until 30 && name != currentName,
            onCancelClick = onDismissClick,
            onActionClick = {
                onDismissClick()
                onActionClick(name.trim())
            }
        )
    }
}

@Composable
fun EnterNumberBottomSheet(
    number: Int?,
    label: String,
    title: String,
    isValid: (String) -> Boolean,
    onActionClick: (Int?) -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(number?.toString() ?: "") }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp)
    ) {
        Text(
            text = title,
            style = Typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { if (it.isDigitsOnly()) text = it },
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

        Spacer(modifier = Modifier.height(36.dp))

        BottomSheetButtonRow(
            actionText = stringResource(id = R.string.confirm),
            actionEnabled = isValid(text) && text.isNotBlank() && text.toInt() != number,
            onCancelClick = onDismissClick,
            onActionClick = {
                onActionClick(text.toIntOrNull() ?: number)
                onDismissClick()
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomSheetButtonRowPreview() {
    ActivityTrackerTheme {
        BottomSheetButtonRow(
            actionText = "Idemo",
            onActionClick = {},
            onCancelClick = {}
        )
    }
}

@Composable
fun trackerOutlinedTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        disabledTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
        cursorColor = MaterialTheme.colorScheme.primary,
        errorTextColor = MaterialTheme.colorScheme.error,
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorContainerColor = MaterialTheme.colorScheme.surface
    )
}

@Preview
@Composable
private fun ConfirmActionBottomSheetPreview() {
    ActivityTrackerTheme {
        ConfirmActionBottomSheet(
            title = "Are you sure you want to logout?",
            subtitle = "This action cannot be undone.",
            actionText = "Confirm",
            actionButtonColor = MaterialTheme.colorScheme.error,
            actionButtonTextColor = MaterialTheme.colorScheme.onError,
            onActionClick = { },
            onDismissClick = { }
        )
    }
}

@Preview
@Composable
private fun ChangeNameBottomSheetPreview() {
    ActivityTrackerTheme {
        ChangeNameBottomSheet(
            currentName = "Johnny Silverhand",
            onActionClick = { },
            onDismissClick = { }
        )
    }
}

@Preview
@Composable
private fun EnterNumberBottomSheetPreview() {
    ActivityTrackerTheme {
        EnterNumberBottomSheet(
            number = 41,
            label = "Weight",
            title = "Update weight",
            isValid = { it.toInt() > 10 },
            onDismissClick = {},
            onActionClick = {}
        )
    }
}
