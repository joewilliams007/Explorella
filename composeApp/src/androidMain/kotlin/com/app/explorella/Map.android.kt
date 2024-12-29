package com.app.explorella

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.util.Log
import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.app.explorella.models.MapState
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay

private var mapViewerState: MutableState<MapView?> = mutableStateOf(null)
private var contextState: MutableState<Context?> = mutableStateOf<Context?>(null)
private var onMarkerClickListener: ((BucketItem) -> Unit)? = null
private var onMapChangeListener: ((MapState) -> Unit)? = null

@SuppressLint("WrongConstant", "NewApi")
@Composable
actual fun mapView(mapState: MapState) {
    val boundingBox = BoundingBox(85.0, 180.0, -85.0, -180.0)
    val minZoomLevel = 3.0
    val maxZoomLevel = 18.0
    val mapView = createOSMDroidMap()
    mapViewerState.value = mapView
    contextState.value = LocalContext.current
    AndroidView({ mapView }) {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.setZoomLevel(mapState.zoom)
        mapView.controller.setCenter(GeoPoint(mapState.geoPoint.latitude,mapState.geoPoint.longitude))
        mapView.setScrollableAreaLimitDouble(boundingBox) // Set scroll limits
        mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER) // Disable zoom controls
        mapView.minZoomLevel = minZoomLevel
        mapView.maxZoomLevel = maxZoomLevel
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
private fun createOSMDroidMap(): MapView {
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

/**
 * Draws a marker at the bucket items position.
 */
actual fun drawMarker(bucketItem: BucketItem) {
    val mapView = mapViewerState.value ?: return
    val context = contextState.value
    bucketItem.latitude ?: return
    bucketItem.longitude ?: return

    val marker = Marker(mapView)
    marker.position = GeoPoint(bucketItem.latitude, bucketItem.longitude)
    marker.title = bucketItem.title
    marker.subDescription = bucketItem.description
    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
    marker.setOnMarkerClickListener { _, _ ->
        onMarkerClickListener?.invoke(bucketItem)
        true
    }

    if (context != null) {
        val bitmap = getBitmapFromVectorDrawable(context, if (bucketItem.complete.toInt() == 1) R.drawable.location_correct else R.drawable.location_wrong)
        marker.icon = BitmapDrawable(context.resources, bitmap)
    }
    mapView.overlays.add(marker)
}

/**
 * Animate zoom to a point.
 */
actual fun zoomMap(geoPoint: com.app.explorella.models.GeoPoint) {
    val mapView = mapViewerState.value
    mapView ?: return
    mapView.controller.animateTo(GeoPoint(geoPoint.latitude,geoPoint.longitude))
    onMapChangeListener?.let { it(MapState(geoPoint, mapView.zoomLevelDouble)) }
}

/**
 * Checks where the user clicks on the map!
 */
actual fun addMapClickListener(onMapClick: (com.app.explorella.models.GeoPoint) -> Unit) {
    val mapView = mapViewerState.value ?: return

    val overlay = object : Overlay() {
        override fun onSingleTapUp(event: MotionEvent, mapView: MapView): Boolean {
            val tapLocation = mapView.projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
            val latitude = tapLocation.latitude
            val longitude = tapLocation.longitude
            Log.d("Map Click", "User clicked at Latitude: $latitude, Longitude: $longitude")
            onMapClick(com.app.explorella.models.GeoPoint(latitude,longitude))
            return true
        }
    }
    mapView.overlays.add(overlay)
}

/**
 * Creates a listener for marker clicks.
 */
actual fun addMarkerClickListener(onMarkerClick: (BucketItem) -> Unit) {
    onMarkerClickListener = onMarkerClick
}

/**
 * Creates a listener for map state changes.
 */
actual fun addMapChangeListener(onMapChange: (MapState) -> Unit) {
    onMapChangeListener = onMapChange

    val mapView = mapViewerState.value ?: return
    mapView.addMapListener(object : MapListener {
        override fun onScroll(event: ScrollEvent?): Boolean {
            onMapChange(MapState(com.app.explorella.models.GeoPoint(mapView.mapCenter.latitude, mapView.mapCenter.longitude),mapView.zoomLevelDouble))
            return true
        }

        override fun onZoom(event: ZoomEvent?): Boolean {
            onMapChange(MapState(com.app.explorella.models.GeoPoint(mapView.mapCenter.latitude, mapView.mapCenter.longitude),mapView.zoomLevelDouble))
            return true
        }
    })
}

/**
 * Clear all marker overlays.
 */
actual fun clearMarkers() {
    val mapView = mapViewerState.value ?: return
    mapView.overlays.forEach {
        if (it is Marker) {
            mapView.overlays.remove(it)
        }
    }
    mapView.invalidate()
}