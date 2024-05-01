package com.rafaelboban.activitytracker.ui.screens.main.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.rafaelboban.activitytracker.ui.components.ButtonWithIcon
import com.rafaelboban.activitytracker.ui.components.ChangeNameDialog
import com.rafaelboban.activitytracker.ui.components.ConfirmActionDialog
import com.rafaelboban.activitytracker.ui.components.DialogScaffold
import com.rafaelboban.activitytracker.ui.components.FullScreenLoadingDialog
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
            ProfileEvent.NameChangeError -> Toast.makeText(context, context.getString(R.string.change_name_error), Toast.LENGTH_LONG).show()
            ProfileEvent.NameChangeSuccess -> Toast.makeText(context, context.getString(R.string.change_name_success), Toast.LENGTH_LONG).show()
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
                is ProfileAction.ConfirmChangeName -> viewModel.changeName(action.name)
                ProfileAction.ConfirmDeleteAccount -> viewModel.deleteAccount()
                ProfileAction.ConfirmLogout -> viewModel.logout()
                ProfileAction.DismissDialog -> viewModel.dismissDialogs()
                ProfileAction.OnChangeNameClick -> viewModel.showDialog(ProfileDialogType.CHANGE_NAME)
                ProfileAction.OnDeleteAccountClick -> viewModel.showDialog(ProfileDialogType.DELETE_ACCOUNT)
                ProfileAction.OnLogoutClick -> viewModel.showDialog(ProfileDialogType.SIGN_OUT)
            }
        }
    )
}

@Composable
private fun ProfileScreen(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit
) {
    FullScreenLoadingDialog(showDialog = state.submitInProgress)

    DialogScaffold(
        showDialog = state.showLogoutDialog || state.showChangeNameDialog || state.showDeleteAccountDialog,
        onDismiss = { onAction(ProfileAction.DismissDialog) }
    ) {
        when {
            state.showChangeNameDialog -> {
                ChangeNameDialog(
                    currentName = state.user.displayName,
                    onActionClick = { name -> onAction(ProfileAction.ConfirmChangeName(name)) },
                    onDismissClick = { onAction(ProfileAction.DismissDialog) }
                )
            }

            state.showDeleteAccountDialog -> {
                ConfirmActionDialog(
                    title = stringResource(id = R.string.confirm_delete_account),
                    subtitle = stringResource(id = R.string.delete_account_warning),
                    actionText = stringResource(id = R.string.confirm),
                    actionButtonColor = MaterialTheme.colorScheme.primary,
                    actionButtonTextColor = MaterialTheme.colorScheme.onPrimary,
                    onActionClick = { onAction(ProfileAction.ConfirmDeleteAccount) },
                    onDismissClick = { onAction(ProfileAction.DismissDialog) }
                )
            }

            else -> {
                ConfirmActionDialog(
                    title = stringResource(id = R.string.confirm_sign_out),
                    actionText = stringResource(id = R.string.confirm),
                    actionButtonColor = MaterialTheme.colorScheme.primary,
                    actionButtonTextColor = MaterialTheme.colorScheme.onPrimary,
                    onActionClick = { onAction(ProfileAction.ConfirmLogout) },
                    onDismissClick = { onAction(ProfileAction.DismissDialog) }
                )
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(color = MaterialTheme.colorScheme.surface)
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

        Spacer(modifier = Modifier.weight(1f))

        ButtonWithIcon(
            text = stringResource(id = R.string.change_name),
            onClick = { onAction(ProfileAction.OnChangeNameClick) },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        )

        ButtonWithIcon(
            text = stringResource(id = R.string.sign_out),
            onClick = { onAction(ProfileAction.OnLogoutClick) },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        )

        ButtonWithIcon(
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
                    displayName = "Johnny Silverhand"
                )
            )
        )
    }
}
