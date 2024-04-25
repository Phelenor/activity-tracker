package com.rafaelboban.activitytracker.ui.screens.login

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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelboban.activitytracker.R
import com.rafaelboban.activitytracker.model.network.PostStatus
import com.rafaelboban.activitytracker.ui.components.FullScreenLoadingDialog
import com.rafaelboban.activitytracker.ui.theme.Typography
import com.rafaelboban.activitytracker.util.CredentialHelper
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(viewModel.postStatus) {
        if (viewModel.postStatus == PostStatus.SUCCESS) {
            onLoginSuccess()
        }
    }

    FullScreenLoadingDialog(showDialog = viewModel.postStatus == PostStatus.IN_PROGRESS)

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
                .height(maxHeight)
        )

        Image(
            painter = painterResource(id = R.drawable.login_background),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .rotate(180f)
                .fillMaxWidth()
                .height(maxHeight / 32)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 32.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Text(
                text = "Fitness Activity Tracker",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 24.sp,
                style = Typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface
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
                    .clickable {
                        viewModel.postStatus = PostStatus.IN_PROGRESS
                        coroutineScope.launch {
                            CredentialHelper.startGoogleLogin(
                                context = context,
                                credentialManager = CredentialManager.create(context),
                                onSuccess = { idToken, nonce -> viewModel.login(idToken, nonce) },
                                onError = { viewModel.postStatus = null }
                            )
                        }
                    }
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
                    text = "Login with Google",
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
    LoginScreen(onLoginSuccess = {})
}
