package com.rafaelboban.activitytracker.ui.components.map

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Polyline
import com.rafaelboban.activitytracker.model.location.LocationTimestamp
import kotlinx.collections.immutable.ImmutableList

@Composable
fun StandardPolylines(locations: ImmutableList<ImmutableList<LocationTimestamp>>) {
    locations.forEach { sequence ->
        Polyline(
            points = sequence.map { location -> LatLng(location.location.location.latitude, location.location.location.longitude) },
            color = MaterialTheme.colorScheme.primary,
            jointType = JointType.BEVEL
        )
    }
}
