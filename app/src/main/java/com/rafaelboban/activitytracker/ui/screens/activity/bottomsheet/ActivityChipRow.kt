package com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafaelboban.activitytracker.R
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography

enum class ActivityTabType(@StringRes val titleRes: Int) {
    DETAILS(R.string.details),
    HEART(R.string.heartrate),
    GOALS(R.string.goals),
    WEATHER(R.string.weather)
}

@Composable
fun ActivityChipRow(
    selectedTab: ActivityTabType,
    onTabSelected: (ActivityTabType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp)
                .padding(bottom = 8.dp)
        ) {
            ActivityTabType.entries.forEach { tab ->
                ActivityTabChip(
                    text = stringResource(id = tab.titleRes),
                    isSelected = tab == selectedTab,
                    onClick = { onTabSelected(tab) }
                )
            }
        }

        HorizontalDivider()
    }
}

@Preview(showBackground = true)
@Composable
private fun ActivityChipRowPreview() {
    ActivityTrackerTheme {
        ActivityChipRow(
            selectedTab = ActivityTabType.DETAILS,
            onTabSelected = {}
        )
    }
}

@Composable
fun ActivityTabChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceContainer
    val borderColor = if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
    val textColor = if (isSelected) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurface

    Text(
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontSize = 16.sp,
        style = Typography.labelLarge,
        color = textColor,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(shape = RoundedCornerShape(16.dp), color = backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)

    )
}

@Preview
@Composable
private fun ActivityTabChipPreview() {
    ActivityTrackerTheme {
        ActivityTabChip(
            text = "Details",
            isSelected = false,
            onClick = {}
        )
    }
}
