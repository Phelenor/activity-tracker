package com.rafaelboban.activitytracker.ui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.rafaelboban.core.theme.mobile.Typography
import kotlinx.serialization.json.JsonNull.content

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
        modifier = modifier
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

@Composable
fun MapMarker(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    showBorder: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .border(shape = CircleShape, width = 2.dp, color = MaterialTheme.colorScheme.background)
            .size(36.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(4.dp)
    ) {
        content()
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

@Preview
@Composable
private fun MapMarkerWithContentPreview() {
    ActivityTrackerTheme {
        MapMarker {
            Text(
                text = "M",
                style = Typography.displayLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1
            )
        }
    }
}
