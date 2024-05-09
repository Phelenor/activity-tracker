package com.rafaelboban.core.shared.connectivity.clients

import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import com.rafaelboban.core.shared.connectivity.model.DeviceType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class WearNodeDiscovery(context: Context) {

    private val capabilityClient = Wearable.getCapabilityClient(context)

    fun observeConnectedDevices(localDeviceType: DeviceType): Flow<Set<Node>> {
        return callbackFlow {
            val remoteCapability = when (localDeviceType) {
                DeviceType.PHONE -> "activity_tracker_wear_app"
                DeviceType.WATCH -> "activity_tracker_mobile_app"
            }

            try {
                val capability = capabilityClient
                    .getCapability(remoteCapability, CapabilityClient.FILTER_REACHABLE)
                    .await()

                send(capability.nodes)
            } catch (e: ApiException) {
                awaitClose()
                return@callbackFlow
            }

            val listener: (CapabilityInfo) -> Unit = { info ->
                trySend(info.nodes)
            }

            capabilityClient.addListener(listener, remoteCapability)

            awaitClose {
                capabilityClient.removeListener(listener)
            }
        }
    }
}
