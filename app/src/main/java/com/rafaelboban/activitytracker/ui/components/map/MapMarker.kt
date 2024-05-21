package com.rafaelboban.activitytracker.ui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rafaelboban.activitytracker.ui.components.applyIf
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme

@Composable
fun MapMarker(
    imageVector: ImageVector?,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    iconTint: Color = MaterialTheme.colorScheme.onPrimary,
    showBorder: Boolean = false
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .applyIf(showBorder) { border(shape = CircleShape, width = 2.dp, color = MaterialTheme.colorScheme.background) }
            .size(36.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(4.dp)
    ) {
        imageVector?.let {
            Icon(
                imageVector = imageVector,
                tint = iconTint,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview
@Composable
private fun MapMarkerPreview() {
    ActivityTrackerTheme {
        MapMarker(
            imageVector = ImageVector.vectorResource(com.rafaelboban.core.theme.R.drawable.ic_finish_flag),
            backgroundColor = MaterialTheme.colorScheme.error,
            iconTint = MaterialTheme.colorScheme.onError
        )
    }
}
