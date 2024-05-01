package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun UserImage(modifier: Modifier = Modifier, imageUrl: String?) {
    val url = imageUrl?.replaceAfterLast("=", "s512-c")

    ImageWithIndicator(
        url = url,
        placeholder = {
            Icon(
                modifier = Modifier.fillMaxSize(),
                imageVector = Icons.Default.AccountCircle,
                tint = MaterialTheme.colorScheme.onPrimary,
                contentDescription = null
            )
        },
        modifier = modifier
            .clip(CircleShape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondary,
                shape = CircleShape
            )
    )
}
