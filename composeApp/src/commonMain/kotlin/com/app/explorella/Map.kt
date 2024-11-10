package com.app.explorella

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

// file: Map.kt

@Composable
expect fun mapView()

expect fun drawMarker(latitude: Double, longitude: Double, title: String, description: String, image: ImageVector?)