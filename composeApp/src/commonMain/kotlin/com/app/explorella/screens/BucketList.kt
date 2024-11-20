package com.app.explorella.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.navigation.NavController
import app.cash.sqldelight.db.SqlDriver
import com.app.explorella.database.BucketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BucketListScreen(
    rootNavController: NavController,
    paddingValues: PaddingValues,
    sqlDriver: SqlDriver
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("0") }
    var longitude by remember { mutableStateOf("0") }
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

        ExtendedFloatingActionButton(
            onClick = { showDialog = true }, // Open dialog on FAB click
            icon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
            },
            text = { Text("Add Item") },
            containerColor = MaterialTheme.colorScheme.primary,
        )

        /**
         * To create the structure of a new table please enter the queries to the file
         * /AndroidStudioProjects/Explorella/composeApp/src/commonMain/sqldelight/com/app/explorella/Database.sq
         *
         * The command ./gradlew :composeApp:generateSqlDelightInterface will then generate the type-safe code for use.
         * For further abstraction, create a viewmodel. Such as e.g. BucketViewModel.
         */

        val viewModel: BucketViewModel = BucketViewModel(
            sqlDriver = sqlDriver
        )

        // Add/Create Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false }, // Close dialog on outside click
                title = {
                    Text(
                        text = "Create New Item",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Enter details for the new item:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // TODO: longitude, latitude validation.
                            viewModel.addBucketEntry(
                                title = title,
                                description = description,
                                priority = 0,
                                icon = null,
                                latitude = latitude.toDoubleOrNull() ?: 0.0,
                                longitude = longitude.toDoubleOrNull() ?: 0.0,
                                timestamp = System.currentTimeMillis()
                            )
                            showDialog = false
                        }
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDialog = false } // Close dialog on dismiss
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        val bucketEntries by viewModel.bucketEntries.collectAsState()

        LazyColumn {
            if (bucketEntries.isEmpty()) {
                item {
                    Text("No items to display", modifier = Modifier.padding(16.dp))
                }
            } else {
                items(bucketEntries) { entry ->
                    Text(
                        modifier = Modifier.clickable { rootNavController.navigate("ToDo/{{$entry.id}}") },
                        text = entry.title
                    )
                }
            }
        }
        LaunchedEffect(bucketEntries) {
            println("Bucket entries: $bucketEntries")
        }
    }
}

