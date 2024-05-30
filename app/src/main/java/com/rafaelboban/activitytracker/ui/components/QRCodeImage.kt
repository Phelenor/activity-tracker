package com.rafaelboban.activitytracker.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.rafaelboban.activitytracker.util.BarcodeHelper.generateQRCode
import kotlin.math.roundToInt

@Composable
fun QRCodeImage(
    code: String,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val pixelSize = with(density) { size.toPx() }.roundToInt()
    val qrCodeBitmap = generateQRCode(code, pixelSize)

    Image(
        modifier = modifier.size(size),
        bitmap = qrCodeBitmap.asImageBitmap(),
        contentDescription = "QR Code"
    )
}
