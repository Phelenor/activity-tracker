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
    private val connectedNode = MutableStateFlow<Node?>(null)
    private val canTrack = MutableStateFlow(false)

    private var isGroupActivity = false
    private var isGroupActivityOwner = false

    val messages = nodeDiscovery
        .observeConnectedDevices(DeviceType.PHONE)
        .flatMapLatest { connectedDevices ->
            val node = connectedDevices.firstOrNull()

            if (node != null && node.isNearby) {
                connectedNode.value = node
                messagingClient.connectToNode(node.id)
            } else {
                flowOf()
            }
        }.onEach { action ->
            if (action == MessagingAction.ConnectionRequest) {
                val canTrackMessage = if (canTrack.value) MessagingAction.CanTrack else MessagingAction.CanNotTrack
                sendMessageToWatch(canTrackMessage)

                if (isGroupActivity) {
                    sendMessageToWatch(MessagingAction.GroupActivityMarker(isGroupActivityOwner))
                }
            }
        }.shareIn(
            applicationScope,
            SharingStarted.Eagerly
        )

    init {
        connectedNode
            .filterNotNull()
            .flatMapLatest { canTrack }
            .onEach { canTrack ->
                sendMessageToWatch(MessagingAction.ConnectionRequest)
                val message = if (canTrack) MessagingAction.CanTrack else MessagingAction.CanNotTrack
                sendMessageToWatch(message)
            }.launchIn(applicationScope)
    }

    suspend fun sendMessageToWatch(action: MessagingAction) {
        messagingClient.sendOrQueueAction(action)
    }

    fun setActivityData(canTrack: Boolean, isGroupActivity: Boolean, isGroupActivityOwner: Boolean) {
        this.canTrack.value = canTrack
        this.isGroupActivity = isGroupActivity
        this.isGroupActivityOwner = isGroupActivityOwner
    }
}
