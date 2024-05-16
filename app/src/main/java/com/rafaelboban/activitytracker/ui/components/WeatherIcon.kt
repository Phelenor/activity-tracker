package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun WeatherIcon(
    code: String,
    modifier: Modifier = Modifier
) {
    val url = "https://openweathermap.org/img/wn/$code@4x.png"

    ImageWithIndicator(
        modifier = modifier,
        url = url,
        placeholder = {
            Icon(
                modifier = Modifier.fillMaxSize(),
                imageVector = Icons.Default.AccountCircle,
                tint = MaterialTheme.colorScheme.onPrimary,
                contentDescription = null
            )
        }
    )
}
