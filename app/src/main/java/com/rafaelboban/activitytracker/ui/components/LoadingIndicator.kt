package com.rafaelboban.activitytracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    indicatorSize: Dp = 64.dp,
    indicatorStrokeWidth: Dp = 8.dp,
    interceptTouchEvents: Boolean = false
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .applyIf(interceptTouchEvents) {
                consumeTouchEvents()
            }
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(indicatorSize),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.inversePrimary,
            strokeWidth = indicatorStrokeWidth,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun FullScreenLoadingDialog(showDialog: Boolean) {
    AnimatedVisibility(
        visible = showDialog,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(200))
    ) {
        Dialog(
            onDismissRequest = { /* Disallow dismissing */ },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            LoadingIndicator()
        }
    }
}
