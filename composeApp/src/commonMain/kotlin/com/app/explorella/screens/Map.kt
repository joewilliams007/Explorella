package com.app.explorella.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.app.explorella.drawMarker
import com.app.explorella.mapView
import jdk.jfr.Description

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    rootNavController: NavController,
    paddingValues: PaddingValues
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

    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        mapView()
    }
    displayItems()
}

fun displayItems() {
    // TODO: Remove BLOB Data and use database entries.
    val points = listOf(
        Point("DHBW","This is DHBW", 49.474358383071454,8.534289721213689),
        Point("Random","This is Random", 32.3,7.0)
    )
    points.forEach {
        println("Element: $it")
        drawMarker(it.latitude, it.longitude,"DHBW","Hiii",null)
    }
}

data class Point(val title: String, val description: String, val latitude: Double, val longitude: Double)
