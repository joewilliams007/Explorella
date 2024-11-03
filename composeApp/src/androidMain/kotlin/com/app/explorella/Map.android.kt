package com.app.explorella

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
actual fun MapView() {
    val boundingBox = BoundingBox(85.0, 180.0, -85.0, -180.0)
    val minZoomLevel = 3.0
    val maxZoomLevel = 18.0
    val mapView = rememberMapViewWithLifecycle()
    val context = LocalContext.current
    AndroidView({ mapView }) {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.setZoomLevel(3.5)
        mapView.setScrollableAreaLimitDouble(boundingBox) // Set scroll limits
        mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER) // Disable zoom controls
        mapView.minZoomLevel = minZoomLevel
        mapView.maxZoomLevel = maxZoomLevel

        val bitmap = getBitmapFromVectorDrawable(context, R.drawable.location_correct)
        val heart = getBitmapFromVectorDrawable(context, R.drawable.heart)

        val marker = Marker(mapView)
        marker.position = GeoPoint(51.5074, -0.1278)
        marker.title = "London"
        marker.icon = BitmapDrawable(context.resources, bitmap)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.overlays.add(marker)

        val markerDHBW = Marker(mapView)
        markerDHBW.position = GeoPoint(49.474358383071454, 8.534289721213689)
        markerDHBW.title = "DHBW"
        markerDHBW.icon = BitmapDrawable(context.resources, heart)
        markerDHBW.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.overlays.add(markerDHBW)
    }
}

fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
    val vectorDrawable = ContextCompat.getDrawable(context, drawableId) as VectorDrawable
    val bitmap = Bitmap.createBitmap(
        vectorDrawable.intrinsicWidth,
        vectorDrawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
    vectorDrawable.draw(canvas)
    return bitmap
}


@Composable
private fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context).apply { clipToOutline = true } }
    val observer = remember { MapViewLifecycleObserver(mapView) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(Unit) {
        lifecycle.addObserver(observer)
        Configuration.getInstance().userAgentValue = "Explorella"
        onDispose { lifecycle.removeObserver(observer) }
    }
    return mapView
}

class MapViewLifecycleObserver(private val mapView: MapView) : DefaultLifecycleObserver {
    override fun onResume(owner: LifecycleOwner) {
        mapView.onResume()
    }
    override fun onPause(owner: LifecycleOwner) {
        mapView.onPause()
    }
}