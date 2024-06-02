package com.rafaelboban.activitytracker.network.ws

import com.rafaelboban.activitytracker.di.NetworkModule.API_BASE_URL
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import timber.log.Timber
import javax.inject.Inject

class WebSocketClient @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private lateinit var webSocket: WebSocket

    private val _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()

    fun connect(path: String) {
        val url = "${API_BASE_URL}$path"
        val listener = object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                Timber.tag("WEBSOCKET").i("Connected to: $url")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                _messages.tryEmit(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                close()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                close()
            }
        }

        webSocket = okHttpClient.newWebSocket(
            request = Request.Builder().url(url).build(),
            listener = listener
        )
    }

    fun send(message: String) {
        webSocket.send(message)
    }

    fun close() {
        webSocket.close(1000, "Client closing.")
    }
}
