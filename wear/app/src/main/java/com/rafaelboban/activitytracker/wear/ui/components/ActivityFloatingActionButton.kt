package com.rafaelboban.activitytracker.wear.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import com.rafaelboban.core.theme.wear.ActivityTrackerWearTheme

@Composable
fun ActivityActionButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary
        )
    ) {
        Icon(
            modifier = Modifier.fillMaxSize(),
            imageVector = icon,
            tint = MaterialTheme.colorScheme.onTertiary,
            contentDescription = null
        )
    }
}

@Preview
@Composable
private fun ActivityActionButtonPreview() {
    ActivityTrackerWearTheme {
        ActivityActionButton(
            icon = Icons.Filled.Pause,
            onClick = {}
        )
    }
}
