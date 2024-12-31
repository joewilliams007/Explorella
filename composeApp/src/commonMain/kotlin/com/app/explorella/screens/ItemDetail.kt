package com.app.explorella.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import app.cash.sqldelight.db.SqlDriver
import com.app.explorella.BucketItem
import com.app.explorella.addMapClickListener
import com.app.explorella.api.Weather
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
import com.app.explorella.navigation.Routes
import java.io.IOException
import java.io.Serializable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    rootNavController: NavController,
    paddingValues: PaddingValues,
    sqlDriver: SqlDriver,
    id: Long
) {
    val bucketViewModel = BucketViewModel(sqlDriver)
    val item: BucketItem = bucketViewModel.getBucketEntry(id)

    // Lokaler State für die Beschreibung
    var localDescription by remember { mutableStateOf(item.description ?: "") }

    // Todos aus dem ViewModel beobachten
    val todoList by bucketViewModel.todoItems.collectAsState()

    // Lokaler State für neues ToDo
    var newTodoText by remember { mutableStateOf("") }

    // Lokaler State für Checkbox
    var isComplete by remember { mutableStateOf(item.complete == 1L) }

    bucketViewModel.loadTodosForBucket(id)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Zurück-Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back Icon",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { if (rootNavController.currentBackStackEntry != null) {
                        rootNavController.popBackStack()
                    } }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Item Details",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Titel mit Checkbox
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = item.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Complete",
                )
                Checkbox(
                    checked = isComplete,
                    onCheckedChange = {
                        isComplete = it
                        bucketViewModel.updateBucketEntry(
                            id = item.id,
                            title = item.title,
                            description = item.description,
                            priority = item.priority ?: 0,
                            icon = item.icon,
                            latitude = item.latitude ?: 0.0,
                            longitude = item.longitude ?: 0.0,
                            complete = if (it) 1 else 0,
                            timestamp = System.currentTimeMillis()
                        )
                    }
                )
            }
        }

        Text(
            text = "Description",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Start)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = localDescription,
                onValueChange = { localDescription = it },
                label = { Text("Description") },
                modifier = Modifier.weight(1f),
                minLines = 1,
                maxLines = 10
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Button zum Speichern der Beschreibung
            Button(onClick = {
                bucketViewModel.updateBucketEntry(
                    id = item.id,
                    title = item.title,
                    description = localDescription,
                    priority = item.priority ?: 0,
                    icon = item.icon,
                    latitude = item.latitude ?: 0.0,
                    longitude = item.longitude ?: 0.0,
                    complete = item.complete,
                    timestamp = System.currentTimeMillis()
                )
            }) {
                Text("Update")
            }
        }

        Text(
            text = "Coordinates",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Start)
        )

        var selectedCoordinate by remember { mutableStateOf<Coordinate?>(null) }
        LaunchedEffect(rootNavController) {
            val savedStateHandle = rootNavController.currentBackStackEntry?.savedStateHandle
            savedStateHandle?.get<Coordinate>("coordinate")?.let { coordinate ->
                selectedCoordinate = coordinate
                savedStateHandle.remove<Coordinate>("coordinate")
            }
        }

        LaunchedEffect(selectedCoordinate) {
            // This block of code will be executed whenever selectedCoordinate changes
            selectedCoordinate?.let {
                // Your code here, for example:
                println("Selected coordinate changed: $it")
                bucketViewModel.updateBucketEntry(
                    id = item.id,
                    title = item.title,
                    description = item.description,
                    priority = item.priority ?: 0,
                    icon = item.icon,
                    latitude = selectedCoordinate!!.latitude,
                    longitude = selectedCoordinate!!.longitude,
                    complete = item.complete,
                    timestamp = System.currentTimeMillis()
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (selectedCoordinate != null) {
                    "${selectedCoordinate!!.latitude}\n${selectedCoordinate!!.longitude}"
                } else if (item.latitude != null && item.latitude != 0.0) {
                    "${item.latitude}\n${item.longitude}"
                } else {
                    "No location set"
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Button(
                onClick = {
                    rootNavController.navigate(Routes.LocationPickerScreen.route)
                },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text("Pick")
            }
        }

        Text(
            text = "Temperature",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Start)
        )

        var weatherText by remember { mutableStateOf("Loading...") }

        LaunchedEffect(selectedCoordinate ?: item) {
            weatherText = try {
                if (selectedCoordinate != null) {
                    val temperature = Weather().getWeather(Coordinate(selectedCoordinate!!.latitude, selectedCoordinate!!.longitude))
                    "$temperature°C"
                } else if (item.latitude != null && item.latitude != 0.0) {
                    item.longitude?.let { Coordinate(item.latitude, it) }
                        ?.let { Weather().getWeather(it) }
                        ?.toString()?.plus("°C") ?: "Invalid coordinates"
                } else {
                    "Please select a location first"
                }
            } catch (e: IOException) {
                "Network error: Please check your connection"
            } catch (e: Exception) {
                "Error: ${e.localizedMessage}"
            }
        }

        Text(
            text = weatherText,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.Start)
        )

        // To-Do Section Header
        Text(
            text = "To-Do List",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Start)
        )

        // Eingabe für neues ToDo
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newTodoText,
                onValueChange = { newTodoText = it },
                label = { Text("New Task") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (newTodoText.isNotBlank()) {
                    bucketViewModel.addTodo(
                        task = newTodoText,
                        bucketId = item.id
                    )
                    newTodoText = ""
                }
            }) {
                Text("Add")
            }
        }
        // Liste der Todos
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            println("Current TodoList: $todoList") // Debugging
            items(todoList) { todo ->
                TodoItemCard(
                    task = todo.task,
                    onDelete = {
                        bucketViewModel.deleteTodo(todo.id)
                        bucketViewModel.loadTodosForBucket(item.id)
                    },
                    onCheckedChange = { isChecked ->
                        println("Set id "+todo.id+" to state "+isChecked)
                        bucketViewModel.setTodoComplete(todo.id,isChecked)
                        bucketViewModel.loadTodosForBucket(item.id)
                    }, todo.complete
                )
            }
        }
    }
}

@Composable
fun TodoItemCard(task: String, onDelete: () -> Unit, onCheckedChange: (Boolean) -> Unit, isCheckedInitial: Long) {
    var isChecked = isCheckedInitial == 1L

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { checked ->
                    isChecked = checked
                    onCheckedChange(checked)
                }
            )
            Text(
                text = task,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete Icon",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onDelete() }
            )
        }
    }
}

@Composable
fun LocationPickerScreen(rootNavController: NavController, paddingValues: PaddingValues, sqlDriver: SqlDriver,) {
    var selectedLocation by remember { mutableStateOf<GeoPoint?>(null) }
    val preferences: PreferencesViewModel = PreferencesViewModel(
        sqlDriver = sqlDriver
    )
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.weight(1f)
        ) {
            mapView(
                MapState(
                    GeoPoint(
                        preferences.getPreference(MAP_LOCATION_LATITUDE, MAP_LOCATION_LATITUDE_DEFAULT).toDouble(),
                        preferences.getPreference(MAP_LOCATION_LONGITUDE, MAP_LOCATION_LONGITUDE_DEFAULT).toDouble()
                    ),
                    preferences.getPreference(MAP_ZOOM_LEVEL, MAP_ZOOM_LEVEL_DEFAULT).toDouble()
                )
            )

            val onMapClick: (GeoPoint) -> Unit = { geoPoint ->
                println("Common code clicked on: ${geoPoint.latitude} ${geoPoint.longitude}")
                selectedLocation = geoPoint
                clearMarkers()
                drawMarker(
                    BucketItem(
                        0,
                        "locationPicker",
                        null,
                        null,
                        null,
                        geoPoint.latitude,
                        geoPoint.longitude,
                        1,
                        null
                    )
                )
            }

            addMapClickListener(onMapClick)
        }

        Button(
            onClick = {
                selectedLocation?.let { geoPoint ->
                    rootNavController.previousBackStackEntry?.savedStateHandle?.set(
                        "coordinate",
                        Coordinate(geoPoint.latitude, geoPoint.longitude)
                    )
                    rootNavController.popBackStack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = selectedLocation != null
        ) {
            Text(
                text = "Use this location",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }

}

data class Coordinate(val latitude: Double, val longitude: Double) : Serializable
