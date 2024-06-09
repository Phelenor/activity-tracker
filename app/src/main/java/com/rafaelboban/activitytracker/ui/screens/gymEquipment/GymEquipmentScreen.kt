package com.rafaelboban.activitytracker.ui.screens.gymEquipment

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rafaelboban.activitytracker.R
import com.rafaelboban.activitytracker.model.gym.GymEquipment
import com.rafaelboban.activitytracker.ui.components.ButtonSecondary
import com.rafaelboban.activitytracker.ui.components.CardColumn
import com.rafaelboban.activitytracker.ui.components.EquipmentTopAppBar
import com.rafaelboban.activitytracker.ui.components.LoadingIndicator
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography

@Composable
fun GymEquipmentScreenRoot(
    navigateUp: () -> Boolean,
    viewModel: GymEquipmentViewModel = hiltViewModel()
) {
    GymEquipmentScreen(
        state = viewModel.state,
        onAction = { action ->
            when (action) {
                GymEquipmentScreenAction.OnBackClick -> navigateUp()
                GymEquipmentScreenAction.OnRetryClick -> viewModel.getEquipment()
            }
        }
    )
}

@Composable
private fun GymEquipmentScreen(
    state: GymEquipmentScreenState,
    onAction: (GymEquipmentScreenAction) -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            state.equipment?.let {
                EquipmentTopAppBar(
                    name = state.equipment.name,
                    activityType = state.equipment.activityType,
                    onBackButtonClick = { onAction(GymEquipmentScreenAction.OnBackClick) }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.equipment != null -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CardColumn(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier.border(width = 1.dp, color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = state.equipment.imageUrl,
                                    contentDescription = null,
                                    error = painterResource(R.drawable.ic_gym),
                                    placeholder = painterResource(R.drawable.ic_gym),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16 / 9f)
                                        .background(color = MaterialTheme.colorScheme.background)
                                )
                            }
                        }

                        CardColumn(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.description),
                                style = Typography.displayMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )

                            Text(
                                text = state.equipment.description,
                                style = Typography.labelLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }

                        state.equipment.videoUrl?.let { videoUrl ->
                            ButtonSecondary(
                                text = stringResource(R.string.watch_video),
                                onClick = {
                                    val uri = Uri.parse(videoUrl)
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }

                state.isLoading -> {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp, horizontal = 8.dp)
                    ) {
                        Text(
                            text = "Equipment info fetch failed. Please try again.",
                            style = Typography.labelLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(Modifier.height(16.dp))

                        ButtonSecondary(
                            text = stringResource(R.string.retry),
                            onClick = { onAction(GymEquipmentScreenAction.OnRetryClick) }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = state.isLoading,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200))
            ) {
                LoadingIndicator(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Preview
@Composable
private fun ActivityOverviewScreenPreview() {
    ActivityTrackerTheme {
        GymEquipmentScreen(
            state = GymEquipmentScreenState(
                isLoading = false,
                equipment = GymEquipment(
                    id = "safafas",
                    name = "Trake",
                    description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris luctus sapien sagittis dolor imperdiet maximus vel nec eros. Vivamus imperdiet velit dui, a tempor orci iaculis eget. Morbi fringilla interdum odio, ac tempus elit congue vel. Ut vitae bibendum ante. Praesent feugiat, urna vitae fringilla congue, tortor nunc lacinia magna, in commodo sapien libero eget sapien. Lorem ipsum dolor sit amet, consectetur adipiscing elit. ",
                    imageUrl = "test",
                    videoUrl = "sagas",
                    activityType = ActivityType.RUN
                )
            ),
            onAction = {}
        )
    }
}
