package com.rafaelboban.activitytracker.ui.components

import android.graphics.Picture
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafaelboban.activitytracker.R
import com.rafaelboban.activitytracker.ui.screens.camera.ScannerType
import com.rafaelboban.activitytracker.util.ShareBitmapHelper
import com.rafaelboban.core.theme.mobile.ActivityTrackerTheme
import com.rafaelboban.core.theme.mobile.Typography

@Composable
fun ShareGroupActivityDialog(
    inviteText: String,
    joinCode: String,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val picture = remember { Picture() }

    Column(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .background(shape = RoundedCornerShape(32.dp), color = MaterialTheme.colorScheme.surfaceBright)
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.share_join_code),
            style = Typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .drawWithCache {
                    val width = this.size.width.toInt()
                    val height = this.size.height.toInt()
                    onDrawWithContent {
                        val pictureCanvas = Canvas(
                            picture.beginRecording(
                                width,
                                height
                            )
                        )

                        draw(this, this.layoutDirection, pictureCanvas, this.size) {
                            this@onDrawWithContent.drawContent()
                        }

                        picture.endRecording()

                        drawIntoCanvas { canvas -> canvas.nativeCanvas.drawPicture(picture) }
                    }
                }
                .align(Alignment.CenterHorizontally)
                .background(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primary)
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    painter = painterResource(com.rafaelboban.core.shared.R.drawable.app_logo_main),
                    tint = Color.Unspecified,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = inviteText,
                    style = Typography.displayLarge,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            QRCodeImage(
                code = ScannerType.GROUP_ACTIVITY.urlFormat + joinCode,
                size = 216.dp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = joinCode,
                style = Typography.displayLarge,
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        DialogButtonRow(
            positiveText = stringResource(R.string.share),
            negativeText = stringResource(R.string.dismiss),
            onCancelClick = onDismissClick,
            onActionClick = { ShareBitmapHelper.sharePicture(context, picture, joinCode) }
        )
    }
}

@Preview
@Composable
private fun ShareGroupActivityDialogPreview() {
    ActivityTrackerTheme {
        ShareGroupActivityDialog(
            joinCode = "23AD23",
            onDismissClick = {},
            inviteText = "Join my Run."
        )
    }
}
