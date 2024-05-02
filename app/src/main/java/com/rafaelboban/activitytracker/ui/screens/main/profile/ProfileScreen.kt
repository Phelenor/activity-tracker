package com.rafaelboban.activitytracker.ui.screens.main.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelboban.activitytracker.R
import com.rafaelboban.activitytracker.model.User
import com.rafaelboban.activitytracker.ui.components.ButtonSecondary
import com.rafaelboban.activitytracker.ui.components.ChangeNameBottomSheet
import com.rafaelboban.activitytracker.ui.components.ConfirmActionBottomSheet
import com.rafaelboban.activitytracker.ui.components.EnterNumberBottomSheet
import com.rafaelboban.activitytracker.ui.components.FullScreenLoadingDialog
import com.rafaelboban.activitytracker.ui.components.LabeledItem
import com.rafaelboban.activitytracker.ui.components.UserImage
import com.rafaelboban.activitytracker.ui.theme.ActivityTrackerTheme
import com.rafaelboban.activitytracker.ui.theme.Typography
import com.rafaelboban.activitytracker.ui.util.ObserveAsEvents
import com.rafaelboban.activitytracker.util.CredentialHelper
import kotlinx.coroutines.launch

@Composable
fun ProfileScreenRoot(
    navigateToLogin: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            ProfileEvent.UserInfoChangeError -> Toast.makeText(context, context.getString(R.string.user_info_change_error), Toast.LENGTH_LONG).show()
            ProfileEvent.UserInfoChangeSuccess -> Toast.makeText(context, context.getString(R.string.user_info_change_success), Toast.LENGTH_LONG).show()
            ProfileEvent.DeleteAccountError -> Toast.makeText(context, context.getString(R.string.delete_account_error), Toast.LENGTH_LONG).show()
            ProfileEvent.LogoutSuccess,
            ProfileEvent.DeleteAccountSuccess -> coroutineScope.launch {
                CredentialHelper.logout(credentialManager)
                navigateToLogin()
            }
        }
    }

    ProfileScreen(
        state = viewModel.state,
        onAction = { action ->
            when (action) {
                ProfileAction.ConfirmDeleteAccount -> viewModel.deleteAccount()
                ProfileAction.ConfirmLogout -> viewModel.logout()
                ProfileAction.DismissDialog -> viewModel.dismissDialogs()
                ProfileAction.OnChangeNameClick -> viewModel.showDialog(ProfileDialogType.CHANGE_NAME)
                ProfileAction.OnDeleteAccountClick -> viewModel.showDialog(ProfileDialogType.DELETE_ACCOUNT)
                ProfileAction.OnLogoutClick -> viewModel.showDialog(ProfileDialogType.SIGN_OUT)
                ProfileAction.OnHeightClick -> viewModel.showDialog(ProfileDialogType.UPDATE_HEIGHT)
                ProfileAction.OnWeightClick -> viewModel.showDialog(ProfileDialogType.UPDATE_WEIGHT)
                is ProfileAction.ConfirmChangeName -> viewModel.updateUser(name = action.name)
                is ProfileAction.ConfirmHeightClick -> viewModel.updateUser(height = action.height)
                is ProfileAction.ConfirmWeightClick -> viewModel.updateUser(weight = action.weight)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreen(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState()
    val showBottomSheet = state.showLogoutDialog || state.showChangeNameDialog || state.showDeleteAccountDialog || state.showWeightDialog || state.showHeightDialog

    val dismissBottomSheet: () -> Unit = {
        coroutineScope.launch {
            bottomSheetState.hide()
            onAction(ProfileAction.DismissDialog)
        }
    }

    FullScreenLoadingDialog(showDialog = state.submitInProgress)

    if (showBottomSheet) {
        ModalBottomSheet(
            sheetState = bottomSheetState,
            onDismissRequest = { onAction(ProfileAction.DismissDialog) }
        ) {
            when {
                state.showChangeNameDialog -> {
                    ChangeNameBottomSheet(
                        currentName = state.user.displayName,
                        onActionClick = { name -> onAction(ProfileAction.ConfirmChangeName(name)) },
                        onDismissClick = dismissBottomSheet
                    )
                }

                state.showDeleteAccountDialog -> {
                    ConfirmActionBottomSheet(
                        title = stringResource(id = R.string.confirm_delete_account),
                        subtitle = stringResource(id = R.string.delete_account_warning),
                        actionText = stringResource(id = R.string.confirm),
                        actionButtonColor = MaterialTheme.colorScheme.error,
                        actionButtonTextColor = MaterialTheme.colorScheme.onError,
                        onActionClick = { onAction(ProfileAction.ConfirmDeleteAccount) },
                        onDismissClick = dismissBottomSheet
                    )
                }

                state.showWeightDialog -> {
                    EnterNumberBottomSheet(
                        number = state.user.weight,
                        label = stringResource(id = R.string.weight),
                        title = stringResource(id = R.string.update_weight),
                        isValid = { weight -> weight.toIntOrNull() in 30..400 || weight.isBlank() },
                        onActionClick = { weight -> weight?.let { onAction(ProfileAction.ConfirmWeightClick(weight)) } },
                        onDismissClick = dismissBottomSheet
                    )
                }

                state.showHeightDialog -> {
                    EnterNumberBottomSheet(
                        number = state.user.height,
                        label = stringResource(id = R.string.height),
                        title = stringResource(id = R.string.update_height),
                        isValid = { weight -> weight.toIntOrNull() in 100..250 || weight.isBlank() },
                        onActionClick = { height -> height?.let { onAction(ProfileAction.ConfirmHeightClick(height)) } },
                        onDismissClick = dismissBottomSheet
                    )
                }

                else -> {
                    ConfirmActionBottomSheet(
                        title = stringResource(id = R.string.confirm_sign_out),
                        actionText = stringResource(id = R.string.confirm),
                        actionButtonColor = MaterialTheme.colorScheme.primary,
                        actionButtonTextColor = MaterialTheme.colorScheme.onPrimary,
                        onActionClick = { onAction(ProfileAction.ConfirmLogout) },
                        onDismissClick = dismissBottomSheet
                    )
                }
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(color = MaterialTheme.colorScheme.background)
            .padding(top = 32.dp, bottom = 16.dp)
    ) {
        UserImage(
            imageUrl = state.user.imageUrl,
            modifier = Modifier
                .border(width = 6.dp, color = MaterialTheme.colorScheme.primary, shape = CircleShape)
                .padding(12.dp)
                .size(148.dp)
                .clip(CircleShape)
                .background(color = MaterialTheme.colorScheme.primary)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = state.user.displayName,
            color = MaterialTheme.colorScheme.onSurface,
            style = Typography.displayLarge
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = state.user.email,
            color = MaterialTheme.colorScheme.onSurface,
            style = Typography.labelLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .background(shape = RoundedCornerShape(32.dp), color = MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(16.dp)
        ) {
            LabeledItem(
                label = stringResource(id = R.string.height),
                value = state.user.height?.let { "${it}cm" } ?: stringResource(id = R.string.add),
                modifier = Modifier
                    .weight(1f)
                    .background(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onAction(ProfileAction.OnHeightClick) }
                    .padding(vertical = 6.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            LabeledItem(
                label = stringResource(id = R.string.weight),
                value = state.user.weight?.let { "${it}kg" } ?: stringResource(id = R.string.add),
                modifier = Modifier
                    .weight(1f)
                    .background(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onAction(ProfileAction.OnWeightClick) }
                    .padding(vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        ButtonSecondary(
            text = stringResource(id = R.string.change_name),
            onClick = { onAction(ProfileAction.OnChangeNameClick) },
            imageVector = Icons.Filled.Edit,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        )

        ButtonSecondary(
            text = stringResource(id = R.string.sign_out),
            onClick = { onAction(ProfileAction.OnLogoutClick) },
            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        )

        ButtonSecondary(
            text = stringResource(id = R.string.delete_account),
            onClick = { onAction(ProfileAction.OnDeleteAccountClick) },
            imageVector = Icons.Default.Delete,
            containerColor = MaterialTheme.colorScheme.error,
            textColor = MaterialTheme.colorScheme.onError,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        )
    }
}

@Preview
@PreviewLightDark
@Composable
private fun ProfileScreenPreview() {
    ActivityTrackerTheme {
        ProfileScreen(
            onAction = {},
            state = ProfileState(
                user = User(
                    id = "213141",
                    email = "test@gmail.com",
                    imageUrl = "https://lh3.googleusercontent.com/a/ACg8ocIkI-iHUZ-RnNOU6tqTO7NPPLQ_pZbVZLV-Ha6Lx8rV6aPk_uc=s96-c",
                    name = "Johnny Silverhand",
                    displayName = "Johnny Silverhand",
                    weight = 83,
                    height = 192
                )
            )
        )
    }
}
