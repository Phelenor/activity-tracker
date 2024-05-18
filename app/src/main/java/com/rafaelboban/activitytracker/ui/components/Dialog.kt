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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.maps.android.compose.MapType
import com.rafaelboban.activitytracker.R
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography

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
    positiveText: String,
    onActionClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
    negativeText: String = "Cancel",
    actionEnabled: Boolean = true,
    actionButtonColor: Color = MaterialTheme.colorScheme.primaryContainer,
    actionButtonTextColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        ButtonPrimary(
            text = negativeText,
            onClick = onCancelClick,
            containerColor = Color.Transparent
        )

        Spacer(modifier = Modifier.width(8.dp))

        ButtonPrimary(
            text = positiveText,
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
fun InfoDialog(
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
            positiveText = actionText,
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
fun SelectMapTypeDialog(
    currentType: MapType,
    onConfirmClick: (MapType) -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mapType by remember { mutableStateOf(currentType) }

    Column(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .background(shape = RoundedCornerShape(32.dp), color = MaterialTheme.colorScheme.surfaceBright)
            .padding(24.dp)
    ) {
        Text(
            text = "Select map type:",
            style = Typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(16.dp))

        listOf(
            MapType.NORMAL,
            MapType.HYBRID,
            MapType.SATELLITE,
            MapType.TERRAIN
        ).forEach { type ->
            RadioButtonRow(
                selected = type == mapType,
                text = type.name.lowercase().capitalize(),
                onSelected = { mapType = type }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        DialogButtonRow(
            positiveText = stringResource(id = R.string.confirm),
            onCancelClick = onDismissClick,
            onActionClick = {
                onConfirmClick(mapType)
                onDismissClick()
            }
        )
    }
}

@Composable
fun SetActivityGoalsDialog(
    onActionClick: () -> Unit,
    onDismissClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var doNotShowAgain by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .background(shape = RoundedCornerShape(32.dp), color = MaterialTheme.colorScheme.surfaceBright)
            .padding(24.dp)
    ) {
        Text(
            text = "Set goals for this activity?",
            style = Typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "This will take you to goal setup.",
            style = Typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        CheckboxRow(
            checked = doNotShowAgain,
            onCheckedChange = { checked -> doNotShowAgain = checked },
            text = "Don't show this reminder again"
        )

        Spacer(modifier = Modifier.height(8.dp))

        DialogButtonRow(
            positiveText = "Yes",
            negativeText = "No",
            onCancelClick = {
                onDismissClick(doNotShowAgain)
            },
            onActionClick = {
                onActionClick()
                onDismissClick(doNotShowAgain)
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DialogButtonRowPreview() {
    ActivityTrackerTheme {
        DialogButtonRow(
            positiveText = "Idemo",
            onActionClick = { },
            onCancelClick = { }
        )
    }
}

@Preview
@Composable
private fun ConfirmActionDialogPreview() {
    ActivityTrackerTheme {
        InfoDialog(
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
private fun SelectMapTypeDialogPreview() {
    ActivityTrackerTheme {
        SelectMapTypeDialog(
            currentType = MapType.NORMAL,
            onDismissClick = {},
            onConfirmClick = {}
        )
    }
}

@Preview
@Composable
private fun SetActivityGoalsDialogPreview() {
    ActivityTrackerTheme {
        SetActivityGoalsDialog(
            onActionClick = {},
            onDismissClick = {}
        )
    }
}
