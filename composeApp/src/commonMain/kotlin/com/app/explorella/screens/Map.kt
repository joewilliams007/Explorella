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
import com.app.explorella.addMapClickListener
import com.app.explorella.addMarkerClickListener
import com.app.explorella.database.BucketViewModel
import com.app.explorella.drawMarker
import com.app.explorella.mapView
import com.app.explorella.models.GeoPoint
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
                .fillMaxSize()
        ) {
            mapView()
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
                            when (selectedIndex) {
                            /**
                             * TODO: Implement different queries when database has needed structure. Add vibration haptic feedback.
                             */
                            0 -> {
                                println("Selected index is 0")
                            }
                                1 -> {
                                    // Handle case for selectedIndex = 1
                                    println("Selected index is 1")
                                }
                                2 -> {
                                    // Handle case for selectedIndex = 2
                                    println("Selected index is 2")
                                }
                                else -> {
                                    // Handle default case
                                    println("Selected index is something else")
                                }
                            }
                        },
                        selected = index == selectedIndex
                    ) {
                        Text(label)
                    }
                }
            }

            pager(sqlDriver, paddingValues)
            listenMap(scaffoldState = scaffoldState, coroutineScope = coroutineScope)
        }

    }
}

@Composable
fun pager(sqlDriver: SqlDriver, paddingValues: PaddingValues) {
    val viewModel: BucketViewModel = BucketViewModel(
        sqlDriver = sqlDriver
    )
    val bucketEntries = viewModel.getAllBucketEntriesDesc()
    displayMarkers(bucketEntries)
    val pagerState = rememberPagerState(pageCount = { bucketEntries.size })

    Row(
        modifier = Modifier
            .fillMaxSize().padding(paddingValues = paddingValues),
        verticalAlignment = Alignment.Bottom // Align content in the Row to the bottom
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

        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }
            .collect { pageIndex ->
                bucketEntries[pageIndex].zoom()
            }
        }
    }
}

/**
 * Listen to map click events by the user.
 */
@OptIn(ExperimentalMaterial3Api::class)
fun listenMap(scaffoldState: BottomSheetScaffoldState, coroutineScope: CoroutineScope) {
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
