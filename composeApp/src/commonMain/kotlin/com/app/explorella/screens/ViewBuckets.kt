package com.app.explorella.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import app.cash.sqldelight.db.SqlDriver
import com.app.explorella.database.BucketViewModel
import com.app.explorella.navigation.Routes
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.rememberCoroutineScope
import com.app.explorella.overpass.Overpass
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewBucketScreen(
    rootNavController: NavController,
    paddingValues: PaddingValues,
    sqlDriver: SqlDriver
) {
    val viewModel: BucketViewModel = BucketViewModel(sqlDriver)
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("49.474358383071454") }
    var longitude by remember { mutableStateOf("8.534289721213689") }
    var searchTerm by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<Overpass.Element>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val overpass = remember { Overpass() }

    // State to trigger recomposition
    var bucketList by remember { mutableStateOf(viewModel.getAllBucketEntriesAsc()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bucket List",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            )
        },
        modifier = Modifier.padding(paddingValues),
        content = { innerPadding ->
            Box(Modifier.padding(innerPadding)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    var isLoading by remember { mutableStateOf(false) }

                    SearchBar(
                        inputField = {
                            SearchBarDefaults.InputField(
                                query = searchTerm,
                                onQueryChange = { searchTerm = it },
                                onSearch = {
                                    // Set loading to true when the search starts
                                    isLoading = true

                                    coroutineScope.launch {
                                        val results = overpass.searchLocations(searchTerm)
                                        searchResults = results
                                        // Set loading to false when the search is complete
                                        isLoading = false
                                    }
                                },
                                expanded = searchActive,
                                onExpandedChange = { searchActive = it },
                                enabled = true,
                                placeholder = { Text("Search for a location") }
                            )
                        },
                        expanded = searchActive,
                        onExpandedChange = { searchActive = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SearchBarDefaults.colors(),
                        tonalElevation = SearchBarDefaults.TonalElevation,
                        shadowElevation = SearchBarDefaults.ShadowElevation,
                        windowInsets = SearchBarDefaults.windowInsets,
                        content = {
                            // Display a progress bar when the search is loading
                            if (isLoading) {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                LazyColumn {
                                    items(searchResults) { result ->
                                        // Each item in the result list can be selected by tapping on it
                                        Text(
                                            text = "${result.tags["name"]} (${result.lat}, ${result.lon})",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    searchTerm = ""
                                                    title = result.tags["name"] ?: "Unknown Location"
                                                    searchActive = false
                                                    latitude = result.lat.toString()
                                                    longitude = result.lon.toString()
                                                }
                                                .padding(16.dp)
                                        )
                                    }

                                }
                            }
                        }
                    )
                    // Eingabezeile für neuen Bucket
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Bucket Title") },
                            modifier = Modifier.weight(1f)
                        )

                        Button(onClick = {
                            if (title.isNotBlank()) {
                                viewModel.addBucketEntry(
                                    title = title,
                                    description = null, // Keine Beschreibung nötig
                                    priority = 0,
                                    icon = null,
                                    latitude = latitude.toDouble(),
                                    longitude = longitude.toDouble(),
                                    timestamp = System.currentTimeMillis()
                                )
                                title = "" // Zurücksetzen des Textfelds
                                bucketList = viewModel.getAllBucketEntriesAsc() // Update the bucket list
                            }
                        }) {
                            Text("Add")
                        }
                    }

                    // Liste der Buckets
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(PaddingValues(16.dp, 0.dp, 16.dp, 8.dp)),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(bucketList) { e ->
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Checkbox(
                                        checked = e.complete == 1L,
                                        onCheckedChange = { isChecked ->
                                            viewModel.updateBucketEntry(
                                                id = e.id,
                                                title = e.title,
                                                description = e.description,
                                                priority = e.priority ?: 0,
                                                icon = e.icon,
                                                latitude = e.latitude ?: 0.0,
                                                longitude = e.longitude ?: 0.0,
                                                complete = if (isChecked) 1 else 0,
                                                timestamp = System.currentTimeMillis()
                                            )
                                            bucketList = viewModel.getAllBucketEntriesAsc() // Update the bucket list
                                        }
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f).clickable {
                                            rootNavController.currentBackStackEntry?.savedStateHandle?.apply {
                                                set("itemId", e.id)
                                            }
                                            rootNavController.navigate(Routes.ItemDetail.route)
                                        }
                                    ) {
                                        Text(
                                            text = e.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Icon",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable {
                                                viewModel.deleteBucketItem(e.id)
                                                bucketList = viewModel.getAllBucketEntriesAsc() // Update the bucket list
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
