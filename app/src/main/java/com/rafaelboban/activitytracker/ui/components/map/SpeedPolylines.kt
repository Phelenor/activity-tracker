package com.rafaelboban.activitytracker.ui.components.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Polyline
import com.rafaelboban.activitytracker.model.location.LocationTimestamp
import com.rafaelboban.activitytracker.model.ui.PolylineUi
import com.rafaelboban.activitytracker.util.PolylineColorHelper
import kotlinx.collections.immutable.ImmutableList

@Composable
fun SpeedPolylines(
    locations: ImmutableList<ImmutableList<LocationTimestamp>>,
    maxSpeed: Float,
    minSpeed: Float
) {
    val polylines = remember(locations) {
        locations.map {
            it.zipWithNext { timestamp1, timestamp2 ->
                PolylineUi(
                    location1 = timestamp1.location.location,
                    location2 = timestamp2.location.location,
                    color = PolylineColorHelper.locationsToColor(
                        location1 = timestamp1,
                        location2 = timestamp2,
                        maxSpeed = maxSpeed,
                        minSpeed = minSpeed
                    )
                )
            }
        }
    }

    polylines.forEach { polyline ->
        polyline.forEach { polylineUi ->
            Polyline(
                color = polylineUi.color,
                jointType = JointType.BEVEL,
                points = listOf(
                    LatLng(polylineUi.location1.latitude, polylineUi.location1.longitude),
                    LatLng(polylineUi.location2.latitude, polylineUi.location2.longitude),
                )
            )
        }
    }
}
