package com.example.indra.ui.theme

import android.os.Bundle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.indra.data.Property
import com.mappls.sdk.maps.MapView
import com.mappls.sdk.maps.MapplsMap
import com.mappls.sdk.maps.OnMapReadyCallback
import com.mappls.sdk.maps.camera.CameraPosition
import com.mappls.sdk.maps.camera.CameraUpdateFactory
import com.mappls.sdk.maps.geometry.LatLng
import com.mappls.sdk.maps.geometry.LatLngBounds
import com.mappls.sdk.maps.annotations.Marker
import com.mappls.sdk.maps.annotations.MarkerOptions

@Composable
private fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, mapView) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) { mapView.onCreate(Bundle()) }
            override fun onStart(owner: LifecycleOwner) { mapView.onStart() }
            override fun onResume(owner: LifecycleOwner) { mapView.onResume() }
            override fun onPause(owner: LifecycleOwner) { mapView.onPause() }
            override fun onStop(owner: LifecycleOwner) { mapView.onStop() }
            override fun onDestroy(owner: LifecycleOwner) { mapView.onDestroy() }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    return mapView
}

@Composable
fun MapplsMapView(
    properties: List<Property>,
    onPropertyClick: (Property) -> Unit,
    modifier: Modifier = Modifier,
    defaultZoom: Double = 12.0
) {
    val mapView = rememberMapViewWithLifecycle()

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            view.getMapAsync(object : OnMapReadyCallback {
                override fun onMapReady(mapplsMap: MapplsMap) {

                    val markerToProperty = mutableMapOf<Marker, Property>()
                    val boundsBuilder = LatLngBounds.Builder()

                    // Add markers and include them in bounds only if created
                    properties.forEach { property ->
                        val marker = mapplsMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(property.latitude, property.longitude))
                                .title(property.name)
                                .snippet(property.address)
                        )
                        marker?.let {
                            markerToProperty[it] = property
                            boundsBuilder.include(LatLng(property.latitude, property.longitude))
                        }
                    }

                    // Adjust camera safely
                    if (markerToProperty.isNotEmpty()) {
                        if (markerToProperty.size > 1) {
                            // Animate camera to include all markers
                            mapplsMap.animateCamera(
                                CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100)
                            )
                        } else {
                            // Zoom to the single marker
                            val first = markerToProperty.values.first()
                            mapplsMap.cameraPosition = CameraPosition.Builder()
                                .target(LatLng(first.latitude, first.longitude))
                                .zoom(defaultZoom)
                                .build()
                        }
                    }

                    // Marker click listener
                    mapplsMap.setOnMarkerClickListener { marker ->
                        markerToProperty[marker]?.let {
                            onPropertyClick(it)
                            true
                        } ?: false
                    }
                }

                override fun onMapError(errorCode: Int, errorMessage: String) {
                    println("Mappls Map failed to load: $errorCode - $errorMessage")
                }
            })
        }
    )
}
