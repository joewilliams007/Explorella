package com.app.explorella.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import app.cash.sqldelight.db.SqlDriver
import com.app.explorella.models.GeoPoint
import com.app.explorella.navigation.Routes
import com.app.explorella.overpass.Overpass
import com.app.explorella.zoomMap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    rootNavController: NavController, paddingValues: PaddingValues
) {
    var name by remember {
        mutableStateOf("")
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Home",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
        var locationQuery: String = ""
        TextField(
            singleLine = true,
            value = name,
            onValueChange = {
                name = it
                locationQuery = it
            },
            label = {
                Text(
                    text = "Where would you like to travel? \uD83E\uDDF3"
                )
            },
            keyboardActions = KeyboardActions(
                onDone = {
                    rootNavController.currentBackStackEntry?.savedStateHandle?.apply {
                        set("locationQuery", locationQuery)
                    }
                    rootNavController.navigate(Routes.LocationSelector.route)
                }
            )
        )
        Button(
            onClick = {
                rootNavController.currentBackStackEntry?.savedStateHandle?.apply {
                    set("locationQuery", locationQuery)
                }
                rootNavController.navigate(Routes.LocationSelector.route)
            }
        ) {
            Text(
                text = "Go!"
            )
        }

        //Settings Button. Entfernt weil kein use? ~LR
        //falls settings existieren: lieber FAB mit settings icon
        /*
        Button(onClick = {
            rootNavController.currentBackStackEntry?.savedStateHandle?.apply {
                set("name", name)
            }
        //rootNavController.navigate(Routes.HomeDetail.route)
        }) {
            Text(
                text = "Settings",
                fontSize = 20.sp
            )
        }*/
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSelectorScreen(
    rootNavController: NavController,
    paddingValues: PaddingValues,
    sqlDriver: SqlDriver,
    locationQuery: String
) {
    var possibleTargets: List<Overpass.Element>? = null
    LaunchedEffect(Unit) {
        possibleTargets = Overpass().searchLocations(locationQuery)
    }

    TopAppBar(
        title = {
            Text(
                text = "Bucket list",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    )
    Spacer(modifier = Modifier.height(20.dp))


    Text("hallo LocationSelectorScreen")


    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(possibleTargets!!) { e ->
            Button(
                onClick = {
                    rootNavController.navigate(Routes.Map.route)
                    zoomMap(
                        GeoPoint(
                            latitude = e.lat,
                            longitude = e.lon
                        )
                    )
                }
            ) {
                Text(e.tags["name"].toString())
            }
        }
    }
}

//  |
//  |   wird das gebraucht? no use und falsche signatur der methode
//  V

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDetailScreen(
    rootNavController: NavController,
    name: String
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Home Detail",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    rootNavController.navigateUp()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                    )
                }
            }
        )
        Text(
            text = "Name = $name",
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
}