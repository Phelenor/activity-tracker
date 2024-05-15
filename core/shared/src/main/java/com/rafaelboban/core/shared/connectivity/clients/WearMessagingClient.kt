package com.rafaelboban.core.shared.connectivity.clients

import android.content.Context
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.rafaelboban.core.shared.connectivity.model.MessagingAction
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WearMessagingClient(context: Context) {

    private val client = Wearable.getMessageClient(context)

    private val messageQueue = mutableListOf<MessagingAction>()
    private var connectedNodeId: String? = null

    fun connectToNode(nodeId: String): Flow<MessagingAction> {
        connectedNodeId = nodeId

        return callbackFlow {
            val listener: (MessageEvent) -> Unit = { event ->
                if (event.path.startsWith(BASE_PATH_MESSAGING_ACTION)) {
                    val json = event.data.decodeToString()
                    val action = Json.decodeFromString<MessagingAction>(json)
                    trySend(action)
                }
            }

            client.addListener(listener)

            messageQueue.forEach {
                sendOrQueueAction(it)
            }

            messageQueue.clear()

            awaitClose {
                client.removeListener(listener)
            }
        }
    }

    suspend fun sendOrQueueAction(action: MessagingAction) {
        connectedNodeId?.let { id ->
            runCatching {
                val json = Json.encodeToString(action)
                client.sendMessage(id, BASE_PATH_MESSAGING_ACTION, json.encodeToByteArray()).await()
            }
        } ?: run {
            runCatching {
                messageQueue.add(action)
            }
        }
    }

    companion object {
        private const val BASE_PATH_MESSAGING_ACTION = "activity-tracker/message"
    }
}
