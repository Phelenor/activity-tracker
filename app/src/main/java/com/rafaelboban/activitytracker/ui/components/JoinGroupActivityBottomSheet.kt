package com.rafaelboban.activitytracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.rafaelboban.activitytracker.R
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography

@Composable
fun JoinGroupActivityBottomSheet(
    isJoiningActivity: Boolean,
    onJoinClick: (String) -> Unit,
    onScanQrCodeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }

    val isValid: (String) -> Boolean = { input ->
        input.length == 8 && input.all { it.isDigit() }
    }

    Box(
        modifier = modifier.height(IntrinsicSize.Min)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.enter_join_code),
                    style = Typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(onClick = onScanQrCodeClick) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.Default.QrCodeScanner,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = text,
                onValueChange = { if (it.isDigitsOnly()) text = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = Typography.bodyLarge,
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                modifier = modifier.fillMaxWidth(),
                colors = trackerOutlinedTextFieldColors(),
                label = {
                    Text(
                        text = stringResource(R.string.join_code),
                        style = Typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )

            Spacer(modifier = Modifier.height(36.dp))

            ButtonPrimary(
                text = stringResource(id = R.string.join),
                enabled = isValid(text),
                onClick = { onJoinClick(text) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        AnimatedVisibility(
            visible = isJoiningActivity,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            LoadingIndicator(modifier = Modifier.fillMaxSize())
        }
    }
}

@Preview
@Composable
private fun JoinGroupActivityBottomSheetPreview() {
    ActivityTrackerTheme {
        JoinGroupActivityBottomSheet(
            isJoiningActivity = false,
            onJoinClick = {},
            onScanQrCodeClick = {}
        )
    }
}
