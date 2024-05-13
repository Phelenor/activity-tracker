package com.rafaelboban.activitytracker.wear.ui.ambient

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.withSaveLayer
import com.rafaelboban.core.shared.utils.F
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes

fun Modifier.ambientMode(
    isAmbientMode: Boolean,
    isBurnInProtectionRequired: Boolean
) = composed {
    val translationX by rememberBurnInTranslation(isAmbientMode, isBurnInProtectionRequired)
    val translationY by rememberBurnInTranslation(isAmbientMode, isBurnInProtectionRequired)

    graphicsLayer {
        this.translationX = translationX
        this.translationY = translationY
    }.ambientGray(isAmbientMode)
}

fun Modifier.ambientGray(isAmbientMode: Boolean): Modifier {
    if (isAmbientMode.not()) return this

    val grayscale = Paint().apply {
        colorFilter = ColorFilter.colorMatrix(
            colorMatrix = ColorMatrix().apply {
                setToSaturation(0f)
            }
        )
    }

    return this.drawWithContent {
        drawIntoCanvas { canvas ->
            canvas.withSaveLayer(size.toRect(), grayscale) {
                drawContent()
            }
        }
    }
}

@Composable
private fun rememberBurnInTranslation(
    isAmbientMode: Boolean,
    isBurnInProtectionRequired: Boolean
): State<Float> {
    val translation = remember { Animatable(0f) }

    LaunchedEffect(isAmbientMode, isBurnInProtectionRequired) {
        if (isAmbientMode && isBurnInProtectionRequired) {
            translation.animateTo(
                targetValue = Random.nextInt(-10, 10).F,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1.minutes.inWholeMilliseconds.toInt(), easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            translation.snapTo(0f)
        }
    }

    return translation.asState()
}
