package com.rafaelboban.activitytracker.ui.screens.main.dashboard

import android.Manifest
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.rafaelboban.activitytracker.R
import com.rafaelboban.activitytracker.ui.components.ActivityTypeSelectBottomSheetBody
import com.rafaelboban.activitytracker.ui.components.ConfigureGroupActivityBottomSheetBody
import com.rafaelboban.activitytracker.ui.components.ControlCard
import com.rafaelboban.activitytracker.ui.components.DialogScaffold
import com.rafaelboban.activitytracker.ui.components.InfoDialog
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.shared.ui.util.ObserveAsEvents
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DashboardScreenRoot(
    navigateToActivity: (ActivityType) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val locationTrackingPermissions = rememberMultiplePermissionsState(
        listOfNotNull(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS else null
        )
    )

    val checkPermissionsAndInvoke: (() -> Unit) -> Unit = { block ->
        if (locationTrackingPermissions.allPermissionsGranted) {
            block()
        } else if (locationTrackingPermissions.shouldShowRationale) {
            viewModel.displayRationaleDialog(isVisible = true)
        } else {
            locationTrackingPermissions.launchMultiplePermissionRequest()
        }
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is DashboardEvent.ActivityCreated -> {
                viewModel.dismissBottomSheet()
                Timber.tag("MARIN").d("${event.groupActivityId} ${event.activityType}")
            }
        }
    }

    DashboardScreen(
        state = viewModel.state,
        onAction = { action ->
            when (action) {
                DashboardAction.DismissBottomSheet -> viewModel.dismissBottomSheet()
                DashboardAction.DismissRationaleDialog -> viewModel.displayRationaleDialog(isVisible = false)
                DashboardAction.RequestPermissions -> locationTrackingPermissions.launchMultiplePermissionRequest()
                DashboardAction.OpenSelectActivityTypeIndividualBottomSheet -> checkPermissionsAndInvoke(viewModel::showSelectActivityBottomSheet)
                DashboardAction.OpenConfigureGroupActivityBottomSheet -> checkPermissionsAndInvoke(viewModel::showConfigureGroupActivityBottomSheet)
                is DashboardAction.CreateGroupActivity -> viewModel.createGroupActivity(action.type, action.estimatedStartTimestamp)
                is DashboardAction.StartIndividualActivity -> {
                    viewModel.dismissBottomSheet()
                    navigateToActivity(action.type)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardState,
    onAction: (DashboardAction) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState()

    val dismissBottomSheet: () -> Unit = {
        coroutineScope.launch {
            bottomSheetState.hide()
            onAction(DashboardAction.DismissBottomSheet)
        }
    }

    DialogScaffold(
        showDialog = state.shouldShowPermissionRationale,
        onDismiss = { onAction(DashboardAction.DismissRationaleDialog) }
    ) {
        InfoDialog(
            title = stringResource(id = R.string.permissions),
            subtitle = stringResource(id = R.string.permissions_rationale_location),
            actionText = stringResource(id = R.string.ok),
            onActionClick = { onAction(DashboardAction.RequestPermissions) },
            onDismissClick = { onAction(DashboardAction.DismissRationaleDialog) }
        )
    }

    if (state.showSelectActivityBottomSheet || state.showConfigureGroupActivityBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = dismissBottomSheet,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            when {
                state.showSelectActivityBottomSheet -> ActivityTypeSelectBottomSheetBody(
                    onClick = { onAction(DashboardAction.StartIndividualActivity(it)) }
                )

                state.showConfigureGroupActivityBottomSheet -> ConfigureGroupActivityBottomSheetBody(
                    isCreatingActivity = state.isCreatingGroupActivity,
                    onClick = { activityType, startTimestamp ->
                        onAction(DashboardAction.CreateGroupActivity(activityType, startTimestamp))
                    }
                )
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 148.dp),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        items(DashboardControl.entries) { control ->
            ControlCard(
                control = control,
                modifier = Modifier.aspectRatio(1f),
                onClick = {
                    when (control) {
                        DashboardControl.INDIVIDUAL_ACTIVITY -> onAction(DashboardAction.OpenSelectActivityTypeIndividualBottomSheet)
                        DashboardControl.CREATE_GROUP_ACTIVITY -> onAction(DashboardAction.OpenConfigureGroupActivityBottomSheet)
                        DashboardControl.JOIN_GROUP_ACTIVITY -> onAction(DashboardAction.OpenSelectActivityTypeIndividualBottomSheet)
                        DashboardControl.JOIN_GYM_ACTIVITY -> onAction(DashboardAction.OpenSelectActivityTypeIndividualBottomSheet)
                        DashboardControl.SCAN_GYM_EQUIPMENT -> onAction(DashboardAction.OpenSelectActivityTypeIndividualBottomSheet)
                    }
                }
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun DashboardScreenPreview() {
    ActivityTrackerTheme {
        DashboardScreen(
            state = DashboardState(),
            onAction = {}
        )
    }
}

enum class DashboardControl(@StringRes val titleRes: Int, @DrawableRes val icon: Int, val offline: Boolean = false) {
    INDIVIDUAL_ACTIVITY(R.string.individual_activity_label, com.rafaelboban.core.shared.R.drawable.ic_run, true),
    CREATE_GROUP_ACTIVITY(R.string.create_group_activity, com.rafaelboban.core.shared.R.drawable.app_logo_main),
    JOIN_GROUP_ACTIVITY(R.string.join_group_activity, R.drawable.ic_person_add),
    JOIN_GYM_ACTIVITY(R.string.join_gym_activity, R.drawable.ic_treadmill_run),
    SCAN_GYM_EQUIPMENT(R.string.get_equipment_info, R.drawable.ic_qr_scanner)
}
