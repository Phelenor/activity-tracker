package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentTopAppBar(
    name: String,
    activityType: ActivityType,
    modifier: Modifier = Modifier,
    onBackButtonClick: () -> Unit
) {
    TopAppBar(
        modifier = modifier,
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
        navigationIcon = {
            IconButton(onClick = onBackButtonClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActivityTypeIcon(
                    modifier = Modifier.size(32.dp),
                    activityType = activityType
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = name,
                    style = Typography.headlineMedium,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    )
}

@PreviewLightDark
@Composable
private fun TrackerTopAppBarPreview() {
    ActivityTrackerTheme {
        EquipmentTopAppBar(
            name = "Treadmill",
            activityType = ActivityType.RUN,
            onBackButtonClick = {}
        )
    }
}
