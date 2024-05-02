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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
fun DialogButtonRow(
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
        ButtonPrimary(
            text = "Cancel",
            onClick = onCancelClick,
            containerColor = Color.Transparent
        )

        Spacer(modifier = Modifier.width(8.dp))

        ButtonPrimary(
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
fun ConfirmActionDialog(
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
            .padding(horizontal = 8.dp)
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

        DialogButtonRow(
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

@Preview(showBackground = true)
@Composable
private fun DialogButtonRowPreview() {
    ActivityTrackerTheme {
        DialogButtonRow(
            actionText = "Idemo",
            onActionClick = { },
            onCancelClick = { }
        )
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
            onActionClick = { },
            onDismissClick = { }
        )
    }
}
