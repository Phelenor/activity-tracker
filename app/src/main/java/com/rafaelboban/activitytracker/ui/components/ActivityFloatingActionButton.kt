package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rafaelboban.activitytracker.R
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme

@Composable
fun ActivityFloatingActionButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showBorder: Boolean = false
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .clickable { onClick() }
            .background(shape = CircleShape, color = MaterialTheme.colorScheme.tertiary.copy(0.3f))
            .padding(if (showBorder) 6.dp else 8.dp)
            .applyIf(showBorder) { border(width = 2.dp, shape = CircleShape, color = MaterialTheme.colorScheme.onPrimary) }
            .background(shape = CircleShape, color = MaterialTheme.colorScheme.tertiary)
            .padding(8.dp)
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
private fun ActivityFloatingActionButtonPreview() {
    ActivityTrackerTheme {
        ActivityFloatingActionButton(
            icon = ImageVector.vectorResource(id = com.rafaelboban.core.theme.R.drawable.ic_finish_flag),
            onClick = {}
        )
    }
}
