package com.app.explorella

import androidx.compose.runtime.Composable
import com.app.explorella.models.GeoPoint
import com.app.explorella.models.MapState

@Composable
expect fun mapView(mapState: MapState)

expect fun drawMarker(bucketItem: BucketItem)

expect fun clearMarkers()

expect fun zoomMap(geoPoint: GeoPoint)

expect fun addMapClickListener(onMapClick: (GeoPoint) -> Unit)

expect fun addMarkerClickListener(onMarkerClick: (BucketItem) -> Unit)

expect fun addMapChangeListener(onMapChange: (MapState) -> Unit)