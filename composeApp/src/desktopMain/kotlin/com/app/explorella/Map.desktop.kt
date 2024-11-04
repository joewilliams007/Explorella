package com.app.explorella

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.vector.ImageVector
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
actual fun mapView() {
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
actual fun drawMarker(
    latitude: Double,
    longitude: Double,
    title: String,
    description: String,
    image: ImageVector?
) {
    val mapViewer = mapViewerState.value ?: return
    val position = GeoPosition(latitude, longitude)
    val waypoint = Waypoint { position }
    waypoints.add(waypoint)
    val waypointPainter = WaypointPainter<Waypoint>()
    waypointPainter.setWaypoints(waypoints)
    mapViewer.setOverlayPainter(waypointPainter)
}