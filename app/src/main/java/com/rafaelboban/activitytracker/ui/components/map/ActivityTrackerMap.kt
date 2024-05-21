@file:OptIn(MapsComposeExperimentalApi::class)

package com.rafaelboban.activitytracker.ui.components.map

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.ktx.awaitSnapshot
import com.rafaelboban.activitytracker.R
import com.rafaelboban.activitytracker.model.location.Location
import com.rafaelboban.activitytracker.model.location.LocationTimestamp
import com.rafaelboban.activitytracker.ui.components.applyIf
import com.rafaelboban.core.shared.model.ActivityType
import com.rafaelboban.core.shared.utils.F
import kotlinx.collections.immutable.ImmutableList

@Composable
fun ActivityTrackerMap(
    currentLocation: Location?,
    locations: ImmutableList<ImmutableList<LocationTimestamp>>,
    cameraLocked: Boolean,
    mapType: MapType,
    activityType: ActivityType,
    maxSpeed: Float,
    onSnapshot: (Bitmap) -> Unit,
    triggerMapSnapshot: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var firstComposition by remember { mutableStateOf(true) }
    val isDarkTheme = isSystemInDarkTheme()
    val mapStyle = remember { MapStyleOptions.loadRawResourceStyle(context, if (isDarkTheme) R.raw.map_style_dark else R.raw.map_style_light) }
    val cameraPositionState = rememberCameraPositionState()
    val markerState = rememberMarkerState()

    var snapshotTriggered by remember { mutableStateOf(false) }

    val userMarkerLatitude by animateFloatAsState(
        targetValue = currentLocation?.latitude?.F ?: 0f,
        animationSpec = tween(durationMillis = 500),
        label = "user_marker_lat"
    )

    val userMarkerLongitude by animateFloatAsState(
        targetValue = currentLocation?.longitude?.F ?: 0f,
        animationSpec = tween(durationMillis = 500),
        label = "user_marker_long"
    )

    val markerPosition = remember(userMarkerLatitude, userMarkerLongitude) {
        LatLng(
            userMarkerLatitude.toDouble(),
            userMarkerLongitude.toDouble()
        )
    }

    LaunchedEffect(markerPosition) {
        markerState.position = markerPosition
    }

    LaunchedEffect(currentLocation, cameraLocked) {
        if (currentLocation != null && cameraLocked) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(currentLocation.latitude, currentLocation.longitude),
                    if (firstComposition) 17f else cameraPositionState.position.zoom
                )
            )

            firstComposition = false
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        contentPadding = PaddingValues(top = 72.dp, bottom = 36.dp, start = 8.dp),
        properties = MapProperties(
            mapStyleOptions = mapStyle,
            mapType = mapType,
            minZoomPreference = 13f
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            tiltGesturesEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
            zoomGesturesEnabled = true,
            scrollGesturesEnabled = cameraLocked.not()
        )
    ) {
        StandardPolylines(locations = locations)

        MapEffect(triggerMapSnapshot) { map ->
            if (triggerMapSnapshot && !snapshotTriggered) {
                snapshotTriggered = true
                map.awaitSnapshot()?.let(onSnapshot)
            }
        }

        currentLocation?.let {
            MarkerComposable(
                currentLocation,
                state = markerState,
                anchor = Offset(0.5f, 0.5f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .applyIf(mapType.hardlyVisible) { border(shape = CircleShape, width = 2.dp, color = MaterialTheme.colorScheme.background) }
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = activityType.drawableRes),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

val MapType.hardlyVisible: Boolean
    get() = this == MapType.SATELLITE || this == MapType.HYBRID
