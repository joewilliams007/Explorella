package com.app.explorella

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import com.app.explorella.models.GeoPoint
import com.app.explorella.models.MapState
import org.jxmapviewer.JXMapViewer
import org.jxmapviewer.OSMTileFactoryInfo
import org.jxmapviewer.viewer.DefaultTileFactory
import org.jxmapviewer.viewer.GeoPosition
import org.jxmapviewer.viewer.Waypoint
import org.jxmapviewer.viewer.WaypointPainter
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.awt.geom.Point2D
import javax.swing.JPanel

var mapViewerState: MutableState<JXMapViewer?> = mutableStateOf(null)

@Composable
actual fun mapView(mapState: MapState) {
    val mapViewer = remember { createJXMapViewer() }
    SwingPanel(
        factory = { mapViewer },
        modifier = Modifier.fillMaxSize()
    )
}

fun createJXMapViewer(): JPanel {
    val mapViewer = JXMapViewer()
    mapViewerState.value = mapViewer
    val tileFactoryInfo = OSMTileFactoryInfo()
    val tileFactory = DefaultTileFactory(tileFactoryInfo)
    mapViewer.tileFactory = tileFactory
    mapViewer.zoom = 5
    mapViewer.addressLocation = GeoPosition(51.5074, -0.1278) // London
    (mapViewer.tileFactory as DefaultTileFactory).setThreadPoolSize(8)
    mapViewer.addMouseWheelListener { e: MouseWheelEvent ->
        mapViewer.zoom -= e.wheelRotation
    }

    // panning
    var lastMousePosition: Pair<Int, Int>? = null
    var isDragging = false
    mapViewer.addMouseListener(object : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
            lastMousePosition = Pair(e.x, e.y) // Capture initial position
            isDragging = true
        }

        override fun mouseReleased(e: MouseEvent) {
            lastMousePosition = null // Reset when released
            isDragging = false
        }
    })

    // dragging
    mapViewer.addMouseMotionListener(object : MouseAdapter() {
        override fun mouseDragged(e: MouseEvent) {
            if (!isDragging) {
                return
            }
            lastMousePosition?.let { (lastX, lastY) ->
                val dx = lastX - e.x // Change in X
                val dy = lastY - e.y // Change in Y

                // Get current geographic center and pixel position2
                val currentCenter = mapViewer.center

                // Calculate new geographic center
                val newPointX = currentCenter.x + dx
                val newPointY = currentCenter.y + dy

                // Convert back to geographic coordinates
                val newCenter = mapViewer.tileFactory.pixelToGeo(Point2D.Double(newPointX, newPointY), mapViewer.zoom)

                // Set new center
                mapViewer.setAddressLocation(GeoPosition(newCenter.latitude, newCenter.longitude))

                // Update last mouse position for the next drag
                lastMousePosition = Pair(e.x, e.y)
            }
        }
    })

    return JPanel().apply {
        layout = java.awt.BorderLayout()
        add(mapViewer)
    }
}

var waypoints = mutableSetOf<Waypoint>()
actual fun drawMarker(bucketItem: BucketItem) {
    bucketItem.latitude ?: return
    bucketItem.longitude ?: return
    val mapViewer = mapViewerState.value ?: return
    val position = GeoPosition(bucketItem.latitude, bucketItem.longitude)
    val waypoint = Waypoint { position }
    waypoints.add(waypoint)
    val waypointPainter = WaypointPainter<Waypoint>()
    waypointPainter.setWaypoints(waypoints)
    mapViewer.setOverlayPainter(waypointPainter)
}

actual fun zoomMap(geoPoint: GeoPoint) {
    val mapViewer = mapViewerState.value ?: return
    mapViewer.zoom = 10
    mapViewer.addressLocation = GeoPosition(geoPoint.latitude,geoPoint.longitude)
}

actual fun addMapClickListener(onMapClick: (GeoPoint) -> Unit) {
    val mapViewer = mapViewerState.value ?: return
    mapViewer.addMouseListener(object : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
            val geoPosition = mapViewer.convertPointToGeoPosition(e.point)
            println("Clicked at Latitude: ${geoPosition.latitude}, Longitude: ${geoPosition.longitude}")

            onMapClick(GeoPoint(geoPosition.latitude,geoPosition.longitude))
        }
    })
}

actual fun addMarkerClickListener(onMarkerClick: (BucketItem) -> Unit) {
    // TODO: Implement desktop marker listener.
}

actual fun addMapChangeListener(onMapChange: (MapState) -> Unit) {
}

actual fun clearMarkers() {
}