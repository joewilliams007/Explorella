package com.app.explorella

import androidx.compose.runtime.Composable
import com.app.explorella.models.GeoPoint

@Composable
expect fun mapView()

expect fun drawMarker(bucketItem: BucketItem)

expect fun zoomMap(geoPoint: GeoPoint)

expect fun addMapClickListener(onMapClick: (GeoPoint) -> Unit)

expect fun addMarkerClickListener(onMarkerClick: (BucketItem) -> Unit)