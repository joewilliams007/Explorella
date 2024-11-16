package com.app.explorella.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import app.cash.sqldelight.db.SqlDriver
import com.app.explorella.BucketItem
import com.app.explorella.addMapChangeListener
import com.app.explorella.addMapClickListener
import com.app.explorella.addMarkerClickListener
import com.app.explorella.clearMarkers
import com.app.explorella.database.BucketViewModel
import com.app.explorella.database.PreferencesViewModel
import com.app.explorella.database.PreferencesViewModel.Companion.MAP_LOCATION_LATITUDE
import com.app.explorella.database.PreferencesViewModel.Companion.MAP_LOCATION_LATITUDE_DEFAULT
import com.app.explorella.database.PreferencesViewModel.Companion.MAP_LOCATION_LONGITUDE
import com.app.explorella.database.PreferencesViewModel.Companion.MAP_LOCATION_LONGITUDE_DEFAULT
import com.app.explorella.database.PreferencesViewModel.Companion.MAP_ZOOM_LEVEL
import com.app.explorella.database.PreferencesViewModel.Companion.MAP_ZOOM_LEVEL_DEFAULT
import com.app.explorella.drawMarker
import com.app.explorella.mapView
import com.app.explorella.models.GeoPoint
import com.app.explorella.models.MapState
import com.app.explorella.res.Res
import com.app.explorella.res.map_options_all
import com.app.explorella.res.map_options_complete
import com.app.explorella.res.map_options_incomplete
import com.app.explorella.zoomMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    rootNavController: NavController,
    paddingValues: PaddingValues,
    sqlDriver: SqlDriver
) {
    val preferences: PreferencesViewModel = PreferencesViewModel(
        sqlDriver = sqlDriver
    )

    val bucket: BucketViewModel = BucketViewModel(
        sqlDriver = sqlDriver
    )

    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            // Restore map state.
            mapView(MapState(GeoPoint(
                preferences.getPreference(MAP_LOCATION_LATITUDE,MAP_LOCATION_LATITUDE_DEFAULT).toDouble(),
                preferences.getPreference(MAP_LOCATION_LONGITUDE,MAP_LOCATION_LONGITUDE_DEFAULT).toDouble()),
                preferences.getPreference(MAP_ZOOM_LEVEL,MAP_ZOOM_LEVEL_DEFAULT).toDouble()))

            var selectedIndex by remember { mutableStateOf(0) }
            val options = listOf(stringResource(Res.string.map_options_all), stringResource(Res.string.map_options_complete), stringResource(Res.string.map_options_incomplete))
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(8.dp)
            ) {
                options.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = options.size
                        ),
                        onClick = {
                            selectedIndex = index
                            clearMarkers()
                            when (selectedIndex) {
                            0 -> {
                                bucket.getAllBucketEntriesDesc()
                            }
                                1 -> {
                                    bucket.getCompleteBucketEntries()
                                }
                                2 -> {
                                    bucket.getIncompleteBucketEntries()
                                }
                                else -> {
                                    bucket.getAllBucketEntriesDesc()
                                }
                            }
                            displayMarkers(bucket.bucketEntries.value)
                        },
                        selected = index == selectedIndex
                    ) {
                        Text(label)
                    }
                }
            }

            pager(sqlDriver, paddingValues, bucket)
            listenMap(scaffoldState = scaffoldState, coroutineScope = coroutineScope, preferences = preferences)
        }
    }
}

@Composable
fun pager(sqlDriver: SqlDriver, paddingValues: PaddingValues, bucket: BucketViewModel) {
    bucket.getAllBucketEntriesDesc()
    val bucketEntries by bucket.bucketEntries.collectAsState()
    if (bucketEntries.isEmpty()) {
        return
    }
    displayMarkers(bucketEntries)
    val pagerState = rememberPagerState(pageCount = { bucketEntries.size })

    Row(
        modifier = Modifier
            .fillMaxSize().padding(paddingValues = paddingValues),
        verticalAlignment = Alignment.Bottom
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(8.dp)
                .height(100.dp)
                .fillMaxWidth()
        ) { pageIndex ->
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
            ) {
                Box(
                    contentAlignment = Alignment.TopStart,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = bucketEntries[pageIndex].title,
                        textAlign = TextAlign.Left,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                    Button(
                        onClick = {
                            /**
                             * TODO: Open page with more details.
                             */
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        Text("details") // TODO: Replace by string Res or change open behaviour when detail page implemented.
                    }
                }
            }
        }
        var firstIgnored = false
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }
            .collect { pageIndex ->
                if (!firstIgnored) {
                    firstIgnored = true
                } else {
                    bucketEntries[pageIndex].zoom()
                }
            }
        }
    }
}

/**
 * Listen to map click events by the user.
 */
@OptIn(ExperimentalMaterial3Api::class)
fun listenMap(scaffoldState: BottomSheetScaffoldState, coroutineScope: CoroutineScope, preferences: PreferencesViewModel) {
    val onMapClick: (GeoPoint) -> Unit = { geoPoint ->
        println("Common code clicked on: ${geoPoint.latitude} ${geoPoint.longitude}")
    }

    addMapClickListener(onMapClick)

    val onMarkerClick: (BucketItem) -> Unit = { bucketItem ->
        println("Common code clicked on: ${bucketItem.latitude} ${bucketItem.longitude}")
        coroutineScope.launch {
            scaffoldState.bottomSheetState.expand()
            bucketItem.zoom()
        }
    }

    addMarkerClickListener(onMarkerClick)
    var lastStateSave = System.currentTimeMillis()
    val onStateChanged: (MapState) -> Unit = { state ->
        val now = System.currentTimeMillis()
        if (now-lastStateSave>300) {
            preferences.setPreference(MAP_LOCATION_LATITUDE,state.geoPoint.latitude.toString())
            preferences.setPreference(MAP_LOCATION_LONGITUDE,state.geoPoint.longitude.toString())
            preferences.setPreference(MAP_ZOOM_LEVEL,state.zoom.toString())
            lastStateSave = now
        }
    }

    addMapChangeListener(onStateChanged)
}

private fun BucketItem.zoom() {
    this.latitude?.let { this.longitude?.let { it1 -> GeoPoint(it, it1) } }?.let { zoomMap(it) }
}

/**
 * Draw the markers.
 */
fun displayMarkers(bucketEntries :List<BucketItem>) {
    bucketEntries.forEach {
        drawMarker(it)
    }
}
