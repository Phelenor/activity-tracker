package com.rafaelboban.activitytracker.ui.screens.activity.bottomsheet

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import kotlin.random.Random

@Composable
fun ActivityBottomSheetContent(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
) {
    var selectedTab by remember { mutableStateOf(ActivityTabType.DETAILS) }

    Column(modifier = modifier.fillMaxWidth()) {
        ActivityChipRow(
            selectedTab = selectedTab,
            onTabSelected = { tab -> selectedTab = tab }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            repeat(10) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(Random.nextInt()))
                )
            }
        }
    }
}

@Preview
@Composable
private fun ActivityBottomSheetContentPreview() {
    ActivityTrackerTheme {
        ActivityBottomSheetContent(
            scrollState = rememberScrollState()
        )
    }
}
