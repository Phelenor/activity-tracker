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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rafaelboban.activitytracker.ui.theme.ActivityTrackerTheme
import com.rafaelboban.activitytracker.ui.theme.Typography

@Composable
fun DialogScaffold(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    dismissBySideEffect: Boolean = true,
    content: @Composable () -> Unit
) {
    if (showDialog) {
        Dialog(
            content = content,
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = dismissBySideEffect,
                dismissOnClickOutside = dismissBySideEffect
            )
        )
    }
}

@Composable
fun ConfirmActionDialog(
    title: String,
    actionText: String,
    onActionClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionButtonColor: Color = MaterialTheme.colorScheme.primaryContainer,
    actionButtonTextColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Column(
        modifier = modifier
            .padding(32.dp)
            .fillMaxWidth()
            .background(shape = RoundedCornerShape(32.dp), color = MaterialTheme.colorScheme.surfaceBright)
            .padding(24.dp)
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            ButtonPrimary(
                text = "Cancel",
                onClick = onDismissClick,
                containerColor = Color.Transparent
            )

            Spacer(modifier = Modifier.width(8.dp))

            ButtonPrimary(
                text = actionText,
                containerColor = actionButtonColor,
                textColor = actionButtonTextColor,
                onClick = {
                    onActionClick()
                    onDismissClick()
                }
            )
        }
    }
}

@Composable
fun ChangeNameDialog(
    currentName: String,
    onActionClick: (String) -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(currentName) }

    Column(
        modifier = modifier
            .padding(32.dp)
            .fillMaxWidth()
            .background(shape = RoundedCornerShape(32.dp), color = MaterialTheme.colorScheme.surfaceBright)
            .padding(24.dp)
    ) {
        Text(
            text = "Enter new name:",
            style = Typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            textStyle = Typography.bodyLarge,
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            isError = name.trim().length !in 3 until 30,
            modifier = modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
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
            ),
            supportingText = {
                Text(
                    text = "Name length longer than 3 characters, and shorter than 30 characters",
                    style = Typography.labelMedium,
                    color = MaterialTheme.colorScheme.inverseSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            },
            label = {
                Text(
                    text = "Name",
                    style = Typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )

        Spacer(modifier = Modifier.height(36.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            ButtonPrimary(
                text = "Cancel",
                onClick = onDismissClick,
                containerColor = Color.Transparent
            )

            Spacer(modifier = Modifier.width(8.dp))

            ButtonPrimary(
                text = "Confirm",
                enabled = name.length in 3 until 30 && name != currentName,
                containerColor = MaterialTheme.colorScheme.primary,
                textColor = MaterialTheme.colorScheme.onPrimary,
                onClick = {
                    onActionClick(name.trim())
                    onDismissClick()
                }
            )
        }
    }
}

@Preview
@Composable
private fun ConfirmActionDialogPreview() {
    ActivityTrackerTheme {
        ConfirmActionDialog(
            title = "Are you sure you want to logout?",
            subtitle = "This action cannot be undone.",
            actionText = "Confirm",
            actionButtonColor = MaterialTheme.colorScheme.error,
            actionButtonTextColor = MaterialTheme.colorScheme.onError,
            onActionClick = { /*TODO*/ },
            onDismissClick = { /*TODO*/ }
        )
    }
}

@Preview
@Composable
private fun ChangeNameDialogPreview() {
    ActivityTrackerTheme {
        ChangeNameDialog(
            currentName = "Johhny Silverhand",
            onActionClick = { /*TODO*/ },
            onDismissClick = { /*TODO*/ }
        )
    }
}
