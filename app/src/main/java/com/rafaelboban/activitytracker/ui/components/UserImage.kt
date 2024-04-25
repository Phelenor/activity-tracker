package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun UserImage(modifier: Modifier = Modifier, imageUrl: String) {
    val url = imageUrl.replace(Regex("uc=s\\d+-c"), "uc=s512-c")

    AsyncImage(
        error = rememberVectorPainter(image = Icons.Filled.AccountCircle),
        placeholder = rememberVectorPainter(image = Icons.Filled.AccountCircle),
        model = url,
        contentDescription = null,
        modifier = modifier
            .clip(CircleShape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondary,
                shape = CircleShape
            )
    )
}
