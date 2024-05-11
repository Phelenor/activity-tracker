package com.rafaelboban.core.shared.connectivity.connectors

import android.util.Log
import com.google.android.gms.wearable.Node
import com.rafaelboban.core.shared.connectivity.clients.WearMessagingClient
import com.rafaelboban.core.shared.connectivity.clients.WearNodeDiscovery
import com.rafaelboban.core.shared.connectivity.model.DeviceType
import com.rafaelboban.core.shared.connectivity.model.MessagingAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.shareIn

@OptIn(ExperimentalCoroutinesApi::class)
class WatchToPhoneConnector(
    applicationScope: CoroutineScope,
    nodeDiscovery: WearNodeDiscovery,
    private val messagingClient: WearMessagingClient
) {

    private val _connectedNode = MutableStateFlow<Node?>(null)
    val connectedNode = _connectedNode.asStateFlow()

    val messages = nodeDiscovery
        .observeConnectedDevices(DeviceType.WATCH)
        .flatMapLatest { connectedNodes ->
            val node = connectedNodes.firstOrNull()

            if (node != null && node.isNearby) {
                _connectedNode.value = node
                messagingClient.connectToNode(node.id)
            } else {
                flowOf()
            }
        }.shareIn(
            applicationScope,
            SharingStarted.Eagerly
        )

    suspend fun sendMessageToPhone(action: MessagingAction) {
        messagingClient.sendOrQueueAction(action)
    }
}
