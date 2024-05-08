@file:SuppressLint("MissingPermission")

package com.rafaelboban.core.tracker.tracking

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.getSystemService
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.rafaelboban.core.tracker.utils.toLocationWithAltitude
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlin.time.Duration.Companion.seconds

class LocationObserver(private val context: Context) {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    fun observeLocation(interval: Long) = callbackFlow {
        val locationManager = checkNotNull(context.getSystemService<LocationManager>())

        var isGpsEnabled = false
        var isNetworkEnabled = false

        while (!isGpsEnabled && !isNetworkEnabled) {
            isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGpsEnabled && !isNetworkEnabled) {
                delay(2.seconds)
            }
        }

        client.lastLocation.addOnSuccessListener { location ->
            location?.let {
                trySend(location.toLocationWithAltitude())
            }
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)

                result.locations.lastOrNull()?.let { location ->
                    trySend(location.toLocationWithAltitude())
                }
            }
        }

        client.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())

        awaitClose {
            client.removeLocationUpdates(locationCallback)
        }
    }
}
