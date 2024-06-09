@file:JvmName("ScannerScreenViewModelKt")

package com.rafaelboban.activitytracker.ui.screens.camera

import android.widget.Toast
import androidx.camera.core.ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.rafaelboban.activitytracker.R
import com.rafaelboban.activitytracker.ui.components.LoadingIndicator
import com.rafaelboban.activitytracker.ui.components.TrackerTopAppBar
import com.rafaelboban.activitytracker.ui.components.camera.CameraPreview
import com.rafaelboban.core.shared.ui.util.ObserveAsEvents
import kotlin.math.min

@Composable
fun ScannerScreenRoot(
    navigateToGroupActivity: (String) -> Unit,
    navigateToEquipmentScreen: (String) -> Unit,
    navigateToGymActivity: (String) -> Unit,
    navigateUp: () -> Boolean,
    viewModel: ScannerScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            ScannerScreenEvent.GroupActivityJoinFailure -> Toast.makeText(context, "Invalid join code", Toast.LENGTH_LONG).show()
            is ScannerScreenEvent.GroupActivityJoinSuccess -> navigateToGroupActivity(event.activityId)
            ScannerScreenEvent.EquipmentScanFailure -> Toast.makeText(context, "Scan error.", Toast.LENGTH_LONG).show()
            is ScannerScreenEvent.EquipmentScanSuccess -> navigateToEquipmentScreen(event.equipmentId)
            ScannerScreenEvent.GymActivityJoinFailure -> Toast.makeText(context, "Join error.", Toast.LENGTH_LONG).show()
            is ScannerScreenEvent.GymActivityJoinSuccess -> navigateToGymActivity(event.activityId)
        }
    }

    ScannerScreen(
        state = viewModel.state,
        onAction = { action ->
            when (action) {
                ScannerScreenAction.OnBackPress -> navigateUp()
                is ScannerScreenAction.OnScanSuccessful -> viewModel.processBarcodeText(action.text)
            }
        }
    )
}

@Composable
private fun ScannerScreen(
    state: ScannerScreenState,
    onAction: (ScannerScreenAction) -> Unit
) {
    val context = LocalContext.current

    val analyzer = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        val barcodeScanner = BarcodeScanning.getClient(options)

        MlKitAnalyzer(
            listOf(barcodeScanner),
            COORDINATE_SYSTEM_VIEW_REFERENCED,
            ContextCompat.getMainExecutor(context)
        ) { result ->
            if (state.isScanningEnabled && state.isCheckingDataValidity.not()) {
                val text = result.getValue(barcodeScanner)?.firstOrNull()?.rawValue

                text?.let {
                    onAction(ScannerScreenAction.OnScanSuccessful(text))
                }
            }
        }
    }

    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
            setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(context), analyzer)
        }
    }

    Scaffold(
        topBar = {
            TrackerTopAppBar(
                showBackButton = true,
                onBackButtonClick = { onAction(ScannerScreenAction.OnBackPress) },
                title = stringResource(R.string.scan_qr_code)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            CameraPreview(
                cameraController = controller,
                modifier = Modifier.fillMaxSize()
            )

            QRCameraOverlay()

            AnimatedVisibility(
                visible = state.isCheckingDataValidity,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200))
            ) {
                LoadingIndicator(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun QRCameraOverlay() {
    val configuration = LocalConfiguration.current
    val borderColor = MaterialTheme.colorScheme.primary
    val minDimension = min(configuration.screenHeightDp, configuration.smallestScreenWidthDp)
    val qrSquareSize = min(250, minDimension - 16)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val overlaySize = qrSquareSize.dp.toPx()
        val borderWidth = 2.dp.toPx()
        val left = (canvasWidth - overlaySize) / 2
        val top = (canvasHeight - overlaySize) / 2

        clipRect(
            left = left,
            top = top,
            right = left + overlaySize,
            bottom = top + overlaySize,
            clipOp = ClipOp.Difference
        ) {
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                size = size
            )
        }

        drawRect(
            color = borderColor,
            topLeft = Offset(left - borderWidth, top - borderWidth),
            size = Size(overlaySize + 2 * borderWidth, overlaySize + 2 * borderWidth),
            style = Stroke(width = borderWidth)
        )
    }
}
