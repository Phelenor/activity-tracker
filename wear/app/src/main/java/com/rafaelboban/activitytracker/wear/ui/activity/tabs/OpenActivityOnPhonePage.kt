package com.rafaelboban.activitytracker.wear.ui.activity.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.SendToMobile
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.rafaelboban.activitytracker.wear.R
import com.rafaelboban.core.theme.wear.ActivityTrackerWearTheme

@Composable
fun OpenActivityOnPhonePage(
    openOnPhone: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp, horizontal = 24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.open_activity_on_phone),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        FilledTonalButton(
            onClick = openOnPhone,
            shape = CircleShape,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        ) {
            Icon(
                modifier = Modifier.fillMaxSize(),
                imageVector = Icons.AutoMirrored.Filled.SendToMobile,
                tint = MaterialTheme.colorScheme.onTertiary,
                contentDescription = null
            )
        }
    }
}

@WearPreviewDevices
@Composable
private fun OpenActivityOnPhonePagePreview() {
    ActivityTrackerWearTheme {
        OpenActivityOnPhonePage(
            openOnPhone = {}
        )
    }
}
