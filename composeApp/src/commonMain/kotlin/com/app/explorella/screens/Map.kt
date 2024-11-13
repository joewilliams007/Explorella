package com.app.explorella.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import app.cash.sqldelight.db.SqlDriver
import com.app.explorella.BucketItem
import com.app.explorella.addMapClickListener
import com.app.explorella.database.BucketViewModel
import com.app.explorella.drawMarker
import com.app.explorella.mapView
import com.app.explorella.models.GeoPoint
import com.app.explorella.zoomMap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    rootNavController: NavController,
    paddingValues: PaddingValues,
    sqlDriver: SqlDriver
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Map",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
    }

    val viewModel: BucketViewModel = BucketViewModel(
        sqlDriver = sqlDriver
    )
    val bucketEntries = viewModel.getAllBucketEntries()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            mapView()
            listenMap()

            displayItems(bucketEntries)

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                items(bucketEntries) { entry ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(2.dp)
                            .clickable {
                                entry.latitude ?: return@clickable
                                entry.longitude ?: return@clickable
                                zoomMap(GeoPoint(entry.latitude,entry.longitude))
                            }
                    ) {
                        Text(
                            text = entry.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .background(Color.White.copy(alpha = 0.8f), shape = RoundedCornerShape(8.dp))                    .padding(16.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }

}

/**
 * Listen to map click events by the user.
 */
fun listenMap() {
    val onMapClick: (GeoPoint) -> Unit = { geoPoint ->
        println("Common code clicked on: ${geoPoint.latitude} ${geoPoint.longitude}")
    }

    addMapClickListener(onMapClick)
}

/**
 * Draw the markers.
 */
fun displayItems(bucketEntries :List<BucketItem>) {
    bucketEntries.forEach {
        println("Element: $it")
        drawMarker(it)
    }
}
