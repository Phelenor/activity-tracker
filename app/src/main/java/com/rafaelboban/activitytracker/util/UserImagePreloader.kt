package com.rafaelboban.activitytracker.util

import android.content.Context
import coil.imageLoader
import coil.request.ImageRequest

object UserImagePreloader {

    fun preload(context: Context, imageUrl: String?) {
        val url = imageUrl?.replaceAfterLast("=", "s512-c")

        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false)
            .build()

        context.imageLoader.enqueue(request)
    }
}
