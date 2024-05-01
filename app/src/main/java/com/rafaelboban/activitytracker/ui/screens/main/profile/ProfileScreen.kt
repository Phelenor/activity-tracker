package com.rafaelboban.activitytracker.ui.screens.main.profile

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelboban.activitytracker.model.User
import com.rafaelboban.activitytracker.model.network.PostStatus
import com.rafaelboban.activitytracker.ui.components.ButtonWithIcon
import com.rafaelboban.activitytracker.ui.components.ChangeNameDialog
import com.rafaelboban.activitytracker.ui.components.ConfirmActionDialog
import com.rafaelboban.activitytracker.ui.components.DialogScaffold
import com.rafaelboban.activitytracker.ui.components.FullScreenLoadingDialog
import com.rafaelboban.activitytracker.ui.components.UserImage
import com.rafaelboban.activitytracker.ui.theme.ActivityTrackerTheme
import com.rafaelboban.activitytracker.ui.theme.Typography
import com.rafaelboban.activitytracker.util.CredentialHelper
import kotlinx.coroutines.runBlocking
import timber.log.Timber

private enum class ProfileDialogType {
    LOGOUT, DELETE_ACCOUNT, CHANGE_NAME
}

@Composable
fun ProfileScreen(
    navigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    var dialogData by remember { mutableStateOf<ProfileDialogType?>(null) }

    FullScreenLoadingDialog(
        showDialog = viewModel.changeNamePostStatus == PostStatus.IN_PROGRESS || viewModel.deleteAccountPostStatus == PostStatus.IN_PROGRESS
    )

    LaunchedEffect(viewModel.deleteAccountPostStatus) {
        if (viewModel.deleteAccountPostStatus == PostStatus.IN_PROGRESS) {
            navigateToLogin()
        }
    }

    DialogScaffold(
        showDialog = dialogData != null,
        onDismiss = { dialogData = null }
    ) {
        val dialogType = checkNotNull(dialogData)

        if (dialogType == ProfileDialogType.CHANGE_NAME) {
            ChangeNameDialog(
                currentName = viewModel.user.displayName,
                onActionClick = viewModel::changeName,
                onDismissClick = { dialogData = null }
            )
        } else {
            val title = when (dialogType) {
                ProfileDialogType.LOGOUT -> "Are you sure you want to logout?"
                ProfileDialogType.DELETE_ACCOUNT -> "Are you sure you want to delete your account?"
                else -> ""
            }

            val subtitle = when (dialogType) {
                ProfileDialogType.DELETE_ACCOUNT -> "This action cannot be undone."
                else -> null
            }

            val action = {
                if (dialogType == ProfileDialogType.DELETE_ACCOUNT) {
                    viewModel.deleteAccount()
                } else {
                    viewModel.logout()
                    // CredentialHelper.logout(CredentialManager.create(context))
                    navigateToLogin()
                }
            }

            ConfirmActionDialog(
                title = title,
                subtitle = subtitle,
                actionText = "Confirm",
                actionButtonColor = MaterialTheme.colorScheme.primary,
                actionButtonTextColor = MaterialTheme.colorScheme.onPrimary,
                onDismissClick = { dialogData = null },
                onActionClick = action
            )
        }
    }

    ProfileContent(
        modifier = modifier,
        user = viewModel.user,
        onLogoutClick = { dialogData = ProfileDialogType.LOGOUT },
        onDeleteAccountClick = { dialogData = ProfileDialogType.DELETE_ACCOUNT },
        onChangeNameClick = { dialogData = ProfileDialogType.CHANGE_NAME }
    )
}

@Composable
private fun ProfileContent(
    user: User,
    onLogoutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onChangeNameClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(top = 32.dp, bottom = 16.dp)
    ) {
        UserImage(
            imageUrl = user.imageUrl,
            modifier = Modifier
                .border(width = 6.dp, color = MaterialTheme.colorScheme.primary, shape = CircleShape)
                .padding(12.dp)
                .size(148.dp)
                .clip(CircleShape)
                .background(color = MaterialTheme.colorScheme.primary)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = user.displayName,
            color = MaterialTheme.colorScheme.onSurface,
            style = Typography.displayLarge
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = user.email,
            color = MaterialTheme.colorScheme.onSurface,
            style = Typography.labelLarge
        )

        Spacer(modifier = Modifier.weight(1f))

        ButtonWithIcon(
            text = "Change Name",
            onClick = onChangeNameClick,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        )

        ButtonWithIcon(
            text = "Logout",
            onClick = onLogoutClick,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        )

        ButtonWithIcon(
            text = "Delete Account",
            onClick = onDeleteAccountClick,
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
        ProfileContent(
            onLogoutClick = {},
            onDeleteAccountClick = {},
            onChangeNameClick = {},
            user = User(
                id = "213141",
                email = "test@gmail.com",
                imageUrl = "https://lh3.googleusercontent.com/a/ACg8ocIkI-iHUZ-RnNOU6tqTO7NPPLQ_pZbVZLV-Ha6Lx8rV6aPk_uc=s96-c",
                name = "Johnny Silverhand",
                displayName = "Johnny Silverhand"
            )
        )
    }
}
