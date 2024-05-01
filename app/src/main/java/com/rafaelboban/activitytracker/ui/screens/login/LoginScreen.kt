package com.rafaelboban.activitytracker.ui.screens.login

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelboban.activitytracker.R
import com.rafaelboban.activitytracker.ui.components.FullScreenLoadingDialog
import com.rafaelboban.activitytracker.ui.theme.ActivityTrackerTheme
import com.rafaelboban.activitytracker.ui.theme.Typography
import com.rafaelboban.activitytracker.ui.util.ObserveAsEvents
import com.rafaelboban.activitytracker.util.CredentialHelper
import kotlinx.coroutines.launch

@Composable
fun LoginScreenRoot(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            is LoginEvent.Error -> Toast.makeText(context, event.error.asString(context), Toast.LENGTH_LONG).show()
            is LoginEvent.Success -> onLoginSuccess()
        }
    }

    LoginScreen(
        isLoading = viewModel.isLoading,
        onAction = { action ->
            when (action) {
                LoginAction.OnLoginClick -> {
                    viewModel.startGoogleLogin()

                    coroutineScope.launch {
                        CredentialHelper.startGoogleLogin(
                            context = context,
                            credentialManager = CredentialManager.create(context),
                            onSuccess = { idToken, nonce -> viewModel.login(idToken, nonce) },
                            onError = { error -> viewModel.finishGoogleLogin(showMessage = error !is GetCredentialCancellationException) }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun LoginScreen(
    isLoading: Boolean,
    onAction: (LoginAction) -> Unit
) {
    FullScreenLoadingDialog(showDialog = isLoading)

    BoxWithConstraints(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
    ) {
        Image(
            painter = painterResource(id = R.drawable.login_background),
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(maxHeight / 1.5f)
        )

        Image(
            painter = painterResource(id = R.drawable.login_background),
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .rotate(180f)
                .fillMaxWidth()
                .height(maxHeight / 2)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 32.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Text(
                text = stringResource(id = R.string.fitness_activity_tracker),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 24.sp,
                style = Typography.displayLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .shadow(elevation = 4.dp, shape = CircleShape)
                    .background(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer)
                    .padding(32.dp)
                    .widthIn(max = 320.dp)
                    .fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "app_logo",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = stringResource(id = R.string.login_welcome),
                style = Typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                modifier = Modifier.padding(vertical = 12.dp),
                text = stringResource(id = R.string.login_ready),
                style = Typography.displayMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
                    .background(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface)
                    .border(width = 1.dp, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primary)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onAction(LoginAction.OnLoginClick) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = stringResource(id = R.string.login_with_google),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = Typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    ActivityTrackerTheme {
        LoginScreen(
            isLoading = false,
            onAction = {}
        )
    }
}
