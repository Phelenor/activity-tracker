package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun UserImage(modifier: Modifier = Modifier, imageUrl: String) {
    val url = imageUrl.replaceAfterLast("=", "s512-c")

    ImageWithIndicator(
        url = url,
        modifier = modifier
            .clip(CircleShape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondary,
                shape = CircleShape
            )
    )
}
