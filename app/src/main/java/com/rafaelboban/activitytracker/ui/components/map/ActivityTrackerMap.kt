@file:OptIn(MapsComposeExperimentalApi::class)

package com.rafaelboban.activitytracker.ui.components.map

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
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
import com.rafaelboban.core.theme.mobile.ColorSuccess
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

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
        modifier = modifier
            .applyIf(!triggerMapSnapshot) { fillMaxSize() }
            .applyIf(triggerMapSnapshot) {
                width(360.dp)
                    .aspectRatio(16 / 9f)
                    .alpha(0f)
            },
        cameraPositionState = cameraPositionState,
        contentPadding = if (triggerMapSnapshot) PaddingValues(0.dp) else PaddingValues(top = 72.dp, bottom = 36.dp, start = 8.dp),
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

                delay(300L)

                val boundsBuilder = LatLngBounds.builder().apply {
                    locations.flatten().forEach { location ->
                        include(
                            LatLng(
                                location.latLong.latitude,
                                location.latLong.longitude
                            )
                        )
                    }
                }

                map.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        boundsBuilder.build(),
                        with(density) { 48.dp.toPx() }.roundToInt()
                    )
                )

                map.setOnCameraIdleListener {
                    scope.launch {
                        delay(100L)
                        map.awaitSnapshot()?.let(onSnapshot)
                    }
                }
            }
        }

        if (snapshotTriggered) {
            val firstLocation = locations.flatten().firstOrNull()
            val lastLocation = locations.flatten().lastOrNull()

            firstLocation?.let {
                lastLocation?.let {
                    MarkerComposable(
                        firstLocation,
                        state = rememberMarkerState(position = LatLng(firstLocation.latLong.latitude, firstLocation.latLong.longitude)),
                        anchor = Offset(0.5f, 0.5f)
                    ) {
                        MapMarker(
                            imageVector = null,
                            backgroundColor = ColorSuccess,
                            showBorder = true
                        )
                    }

                    MarkerComposable(
                        lastLocation,
                        state = rememberMarkerState(position = LatLng(lastLocation.latLong.latitude, lastLocation.latLong.longitude)),
                        anchor = Offset(0.5f, 0.5f)
                    ) {
                        MapMarker(
                            imageVector = ImageVector.vectorResource(com.rafaelboban.core.theme.R.drawable.ic_finish_flag),
                            backgroundColor = MaterialTheme.colorScheme.error,
                            iconTint = MaterialTheme.colorScheme.onError,
                            showBorder = true
                        )
                    }
                }
            }
        } else {
            currentLocation?.let {
                MarkerComposable(
                    currentLocation,
                    state = markerState,
                    anchor = Offset(0.5f, 0.5f)
                ) {
                    MapMarker(
                        imageVector = ImageVector.vectorResource(id = activityType.drawableRes),
                        showBorder = mapType.hardlyVisible
                    )
                }
            }
        }
    }
}

val MapType.hardlyVisible: Boolean
    get() = this == MapType.SATELLITE || this == MapType.HYBRID
