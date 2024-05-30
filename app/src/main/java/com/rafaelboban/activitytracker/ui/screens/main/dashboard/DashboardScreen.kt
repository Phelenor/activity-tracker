package com.rafaelboban.activitytracker.ui.screens.main.dashboard

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.rafaelboban.activitytracker.R
import com.rafaelboban.activitytracker.model.network.GroupActivity
import com.rafaelboban.activitytracker.ui.components.ActivityTypeSelectBottomSheetBody
import com.rafaelboban.activitytracker.ui.components.ConfigureGroupActivityBottomSheetBody
import com.rafaelboban.activitytracker.ui.components.ControlCard
import com.rafaelboban.activitytracker.ui.components.DialogScaffold
import com.rafaelboban.activitytracker.ui.components.InfoDialog
import com.rafaelboban.activitytracker.ui.components.JoinGroupActivityBottomSheet
import com.rafaelboban.activitytracker.ui.components.PendingActivityCard
import com.rafaelboban.activitytracker.ui.screens.camera.ScannerType
import com.rafaelboban.core.shared.model.ActivityStatus
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.shared.ui.util.ObserveAsEvents
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DashboardScreenRoot(
    navigateToActivity: (ActivityType) -> Unit,
    navigateToQRCodeScanner: (ScannerType) -> Unit,
    navigateToGroupActivity: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    var permissionGrantedPendingAction = remember { { } }

    val locationTrackingPermissions = rememberMultiplePermissionsState(
        permissions = listOfNotNull(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS else null
        ),
        onPermissionsResult = {
            permissionGrantedPendingAction()
            permissionGrantedPendingAction = {}
        }
    )

    val cameraPermission = rememberPermissionState(
        permission = Manifest.permission.CAMERA,
        onPermissionResult = {
            permissionGrantedPendingAction()
            permissionGrantedPendingAction = {}
        }
    )

    val checkLocationPermissionsAndInvoke: (() -> Unit) -> Unit = { block ->
        if (locationTrackingPermissions.allPermissionsGranted) {
            viewModel.dismissBottomSheet()
            block()
        } else if (locationTrackingPermissions.shouldShowRationale) {
            viewModel.displayLocationRationaleDialog(isVisible = true)
        } else {
            permissionGrantedPendingAction = block
            locationTrackingPermissions.launchMultiplePermissionRequest()
        }
    }

    val checkCameraPermissionAndInvoke: (() -> Unit) -> Unit = { block ->
        if (cameraPermission.status.isGranted) {
            viewModel.dismissBottomSheet()
            block()
        } else if (cameraPermission.status.shouldShowRationale) {
            viewModel.displayCameraPermissionRationale(isVisible = true)
        } else {
            permissionGrantedPendingAction = block
            cameraPermission.launchPermissionRequest()
        }
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            DashboardEvent.GroupActivityCreationError -> Toast.makeText(context, context.getString(R.string.activity_creation_error), Toast.LENGTH_LONG).show()
            DashboardEvent.GroupActivityJoinError -> Toast.makeText(context, context.getString(R.string.activity_join_error), Toast.LENGTH_LONG).show()
            is DashboardEvent.GroupActivityCreated -> {
                viewModel.getPendingActivities()
                viewModel.dismissBottomSheet()
                navigateToGroupActivity(event.groupActivityId)
            }

            is DashboardEvent.JoinActivitySuccess -> {
                viewModel.getPendingActivities()
                viewModel.dismissBottomSheet()
                navigateToGroupActivity(event.groupActivityId)
            }
        }
    }

    DashboardScreen(
        state = viewModel.state,
        onAction = { action ->
            when (action) {
                DashboardAction.DismissBottomSheet -> viewModel.dismissBottomSheet()
                DashboardAction.DismissRationaleDialog -> viewModel.displayLocationRationaleDialog(isVisible = false)
                DashboardAction.RequestPermissions -> locationTrackingPermissions.launchMultiplePermissionRequest()
                DashboardAction.OpenSelectActivityTypeIndividualBottomSheet -> checkLocationPermissionsAndInvoke(viewModel::showSelectActivityBottomSheet)
                DashboardAction.OpenConfigureGroupActivityBottomSheet -> checkLocationPermissionsAndInvoke(viewModel::showConfigureGroupActivityBottomSheet)
                DashboardAction.OpenJoinGroupActivityBottomSheet -> checkLocationPermissionsAndInvoke(viewModel::showJoinGroupActivityBottomSheet)
                DashboardAction.OpenQRCodeScanner -> checkCameraPermissionAndInvoke { navigateToQRCodeScanner(ScannerType.GROUP_ACTIVITY) }
                is DashboardAction.OnPendingActivityClick -> navigateToGroupActivity(action.groupActivityId)
                is DashboardAction.OnPendingActivityDeleteClick -> viewModel.deletePendingActivity(action.groupActivityId)
                is DashboardAction.JoinGroupActivity -> viewModel.joinGroupActivity(action.joinCode)
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
    val lazyGridState = rememberLazyGridState()
    val bottomSheetState = rememberModalBottomSheetState()

    var firstLoad = remember { true }

    val dismissBottomSheet: () -> Unit = {
        coroutineScope.launch {
            bottomSheetState.hide()
            onAction(DashboardAction.DismissBottomSheet)
        }
    }

    LaunchedEffect(state.pendingActivities) {
        if (firstLoad) {
            firstLoad = false
            lazyGridState.animateScrollToItem(0)
        }
    }

    DialogScaffold(
        showDialog = state.shouldShowLocationPermissionRationale || state.shouldShowCameraPermissionRationale,
        onDismiss = { onAction(DashboardAction.DismissRationaleDialog) }
    ) {
        val subtitleRes = if (state.shouldShowLocationPermissionRationale) R.string.permissions_location_rationale_location else R.string.permissions_camera_rationale_location

        InfoDialog(
            title = stringResource(id = R.string.permissions),
            subtitle = stringResource(id = subtitleRes),
            actionText = stringResource(id = R.string.ok),
            onActionClick = { onAction(DashboardAction.RequestPermissions) },
            onDismissClick = { onAction(DashboardAction.DismissRationaleDialog) }
        )
    }

    if (state.showSelectActivityBottomSheet || state.showConfigureGroupActivityBottomSheet || state.showJoinGroupActivityBottomSheet) {
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

                state.showJoinGroupActivityBottomSheet -> JoinGroupActivityBottomSheet(
                    isJoiningActivity = state.isJoiningGroupActivity,
                    onJoinClick = { joinCode -> onAction(DashboardAction.JoinGroupActivity(joinCode)) },
                    onScanQrCodeClick = { onAction(DashboardAction.OpenQRCodeScanner) }
                )
            }
        }
    }

    LazyVerticalGrid(
        state = lazyGridState,
        columns = GridCells.Adaptive(minSize = 148.dp),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        if (state.pendingActivities.isNotEmpty()) {
            item(
                span = { GridItemSpan(maxLineSpan) },
                key = "scheduled_activities_header"
            ) {
                Text(
                    text = stringResource(R.string.scheduled_activities),
                    style = Typography.displayLarge,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .padding(vertical = 8.dp, horizontal = 8.dp)
                )
            }
        }

        items(
            items = state.pendingActivities,
            span = { GridItemSpan(maxLineSpan) },
            key = { it.id }
        ) { activity ->
            PendingActivityCard(
                groupActivity = activity,
                navigateToGroupActivity = { onAction(DashboardAction.OnPendingActivityClick(activity.id)) },
                onDeleteClick = { onAction(DashboardAction.OnPendingActivityDeleteClick(activity.id)) },
            )
        }

        if (state.pendingActivities.isNotEmpty()) {
            item(
                span = { GridItemSpan(maxLineSpan) },
                key = "list_margin"
            ) {
                Spacer(Modifier.height(32.dp))
            }
        }

        items(
            DashboardControl.entries,
            key = { it }
        ) { control ->
            ControlCard(
                control = control,
                modifier = Modifier.aspectRatio(1f),
                onClick = {
                    when (control) {
                        DashboardControl.INDIVIDUAL_ACTIVITY -> onAction(DashboardAction.OpenSelectActivityTypeIndividualBottomSheet)
                        DashboardControl.CREATE_GROUP_ACTIVITY -> onAction(DashboardAction.OpenConfigureGroupActivityBottomSheet)
                        DashboardControl.JOIN_GROUP_ACTIVITY -> onAction(DashboardAction.OpenJoinGroupActivityBottomSheet)
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
            state = DashboardState(
                pendingActivities = List(3) { i ->
                    GroupActivity(
                        id = "id$i",
                        activityType = ActivityType.RUN,
                        startedUsers = emptyList(),
                        joinedUsers = emptyList(),
                        activeUsers = emptyList(),
                        joinCode = "AD2323",
                        status = ActivityStatus.IN_PROGRESS,
                        userOwnerId = "sdadasd",
                        startTimestamp = 31241412,
                        userOwnerName = "Rafael"
                    )
                }.toImmutableList()
            ),
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
