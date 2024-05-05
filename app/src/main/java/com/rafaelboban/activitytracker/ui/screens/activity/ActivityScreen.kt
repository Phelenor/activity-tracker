package com.rafaelboban.activitytracker.ui.screens.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelboban.activitytracker.R
import com.rafaelboban.activitytracker.model.ActivityType
import com.rafaelboban.activitytracker.model.location.Location
import com.rafaelboban.activitytracker.ui.components.ActivityDataColumn
import com.rafaelboban.activitytracker.ui.components.ActivityTrackerMap
import com.rafaelboban.activitytracker.ui.screens.activity.components.ActivityTopAppBar
import com.rafaelboban.activitytracker.ui.theme.ActivityTrackerTheme

@Composable
fun ActivityScreenRoot(
    navigateUp: () -> Boolean,
    viewModel: ActivityViewModel = hiltViewModel()
) {
    ActivityScreen(
        state = viewModel.state,
        onAction = { action ->
            when (action) {
                ActivityAction.OnBackClick -> navigateUp()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    state: ActivityState,
    onAction: (ActivityAction) -> Unit
) {
    val density = LocalDensity.current
    val navigationBarPadding = with(density) { WindowInsets.navigationBars.getBottom(density).toDp() }

    BottomSheetScaffold(
        sheetPeekHeight = 36.dp + navigationBarPadding,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        sheetContent = {
            Spacer(modifier = Modifier.height(300.dp))
        }
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxWidth()
        ) {
            val (infoCard, map) = createRefs()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .zIndex(1f)
                    .constrainAs(infoCard) {
                        top.linkTo(parent.top)
                        width = Dimension.matchParent
                    }
            ) {
                ActivityTopAppBar(
                    activityType = ActivityType.RUN,
                    onBackClick = { onAction(ActivityAction.OnBackClick) },
                    gpsOk = true
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 12.dp)
                ) {
                    ActivityDataColumn(
                        modifier = Modifier.weight(1f),
                        title = stringResource(id = R.string.duration),
                        value = "01:02:23",
                        icon = Icons.Outlined.Timer
                    )

                    VerticalDivider(modifier = Modifier.height(24.dp))

                    ActivityDataColumn(
                        modifier = Modifier.weight(1f),
                        title = stringResource(id = R.string.distance),
                        value = "3.23",
                        unit = "km",
                        icon = Icons.AutoMirrored.Outlined.TrendingUp
                    )

                    VerticalDivider(modifier = Modifier.height(24.dp))

                    ActivityDataColumn(
                        modifier = Modifier.weight(1f),
                        title = stringResource(id = R.string.speed),
                        value = "9.3",
                        unit = "km/h",
                        icon = Icons.Outlined.Speed
                    )
                }
            }

            ActivityTrackerMap(
                currentLocation = state.currentLocation,
                modifier = Modifier.constrainAs(map) {
                    top.linkTo(infoCard.bottom, margin = (-16).dp)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.matchParent
                    height = Dimension.fillToConstraints
                }
            )
        }
    }
}

@Preview(widthDp = 360)
@PreviewLightDark
@Composable
private fun ActivityScreenPreview() {
    ActivityTrackerTheme {
        ActivityScreen(
            state = ActivityState(),
            onAction = {}
        )
    }
}
