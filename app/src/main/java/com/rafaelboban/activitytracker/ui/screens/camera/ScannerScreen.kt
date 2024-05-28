@file:JvmName("ScannerScreenViewModelKt")

package com.rafaelboban.activitytracker.ui.screens.camera

import androidx.camera.core.ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.rafaelboban.activitytracker.ui.components.TrackerTopAppBar
import com.rafaelboban.activitytracker.ui.components.camera.CameraPreview

@Composable
fun ScannerScreenRoot(
    navigateUp: () -> Boolean,
    viewModel: ScannerScreenViewModel = hiltViewModel()
) {
    ScannerScreen(
        scanningEnabled = viewModel.scanningEnabled,
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
    scanningEnabled: Boolean,
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
            if (scanningEnabled) {
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
                title = "Scan QR Code"
            )
        }
    ) { padding ->
        CameraPreview(
            cameraController = controller,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        )
    }
}
