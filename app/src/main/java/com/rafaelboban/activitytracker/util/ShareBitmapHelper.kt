package com.rafaelboban.activitytracker.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Picture
import android.graphics.drawable.PictureDrawable
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareBitmapHelper {

    fun sharePicture(context: Context, picture: Picture, fileName: String) {
        val bitmap = convertPictureToBitmap(picture)
        val imageUri = saveBitmapToCache(context, bitmap, fileName)
        shareImageUri(context, imageUri)
    }

    fun convertPictureToBitmap(picture: Picture): Bitmap {
        val pictureDrawable = PictureDrawable(picture)
        val bitmap = Bitmap.createBitmap(pictureDrawable.intrinsicWidth, pictureDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawPicture(pictureDrawable.picture)
        return bitmap
    }

    fun saveBitmapToCache(context: Context, bitmap: Bitmap, fileName: String): Uri {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "${fileName}.png")
        val fileOutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    fun shareImageUri(context: Context, uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            clipData = ClipData.newRawUri("", uri)
            putExtra(Intent.EXTRA_STREAM, uri)
        }

        val intent = Intent.createChooser(shareIntent, null)

        context.startActivity(intent)
    }
}
