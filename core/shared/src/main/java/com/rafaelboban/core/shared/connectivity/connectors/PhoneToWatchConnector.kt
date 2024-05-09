package com.rafaelboban.core.shared.connectivity.connectors

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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn

@OptIn(ExperimentalCoroutinesApi::class)
class PhoneToWatchConnector(
    applicationScope: CoroutineScope,
    nodeDiscovery: WearNodeDiscovery,
    private val messagingClient: WearMessagingClient
) {

    private val _connectedNode = MutableStateFlow<Node?>(null)
    val connectedDevice = _connectedNode.asStateFlow()

    private val isTrackable = MutableStateFlow(false)

    val messages = nodeDiscovery
        .observeConnectedDevices(DeviceType.PHONE)
        .flatMapLatest { connectedDevices ->
            val node = connectedDevices.firstOrNull()

            if (node != null && node.isNearby) {
                _connectedNode.value = node
                messagingClient.connectToNode(node.id)
            } else {
                flowOf()
            }
        }.onEach { action ->
            if (action == MessagingAction.ConnectionRequest) {
                if (isTrackable.value) {
                    sendMessageToWatch(MessagingAction.CanTrack)
                } else {
                    sendMessageToWatch(MessagingAction.CanNotTrack)
                }
            }
        }.shareIn(
            applicationScope,
            SharingStarted.Eagerly
        )

    init {
        _connectedNode
            .filterNotNull()
            .flatMapLatest { isTrackable }
            .onEach { isTrackable ->
                sendMessageToWatch(MessagingAction.ConnectionRequest)
                val action = if (isTrackable) MessagingAction.CanTrack else MessagingAction.CanNotTrack
                sendMessageToWatch(action)
            }.launchIn(applicationScope)
    }

    suspend fun sendMessageToWatch(action: MessagingAction) {
        messagingClient.sendOrQueueAction(action)
    }

    fun setIsTrackable(isTrackable: Boolean) {
        this.isTrackable.value = isTrackable
    }
}
