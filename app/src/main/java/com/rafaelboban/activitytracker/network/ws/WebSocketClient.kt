package com.rafaelboban.activitytracker.network.ws

import com.rafaelboban.activitytracker.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import timber.log.Timber
import javax.inject.Inject

private data class WebSocketConnection(
    val webSocket: WebSocket,
    val messages: MutableSharedFlow<String>
)

class WebSocketClient @Inject constructor(
    private val okHttpClient: OkHttpClient
) {

    private val connections = mutableMapOf<String, WebSocketConnection>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun connect(url: String): SharedFlow<String> {
        synchronized(url) {
            connections[url]?.let { connection ->
                return connection.messages
            }

            val messages = MutableSharedFlow<String>()
            val listener = createWebSocketListener(url, messages)

            val webSocket = okHttpClient.newWebSocket(
                request = Request.Builder().url("${BuildConfig.SERVER_URL}$url").build(),
                listener = listener
            )

            connections[url] = WebSocketConnection(webSocket, messages)

            return messages
        }
    }

    fun send(url: String, message: String) {
        synchronized(url) {
            connections[url]?.webSocket?.send(message) ?: Timber.tag("WebSocket").e("WebSocket is not connected to $url.")
        }
    }

    fun close(url: String) {
        synchronized(url) {
            connections[url]?.webSocket?.close(1000, "Client closing.")
            connections.remove(url)
        }
    }

    private fun createWebSocketListener(url: String, messages: MutableSharedFlow<String>) = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
            Timber.tag("WebSocket").i("Connected to: $url")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            scope.launch {
                messages.emit(text)
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Timber.tag("WebSocket").i("Disconnected from by peer: $url")
            webSocket.close(1000, null)
            close(url)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
            Timber.tag("WebSocket").e(t, "WebSocket connection failed for $url")
            close(url)
        }
    }
}
