package com.app.explorella.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import com.app.explorella.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewBucketScreen(
    rootNavController: NavController,
    paddingValues: PaddingValues,
    sqlDriver: SqlDriver
) {
    val viewModel: BucketViewModel = BucketViewModel(sqlDriver)
    val newBucketTitle = remember { mutableStateOf("") } // Lokaler State für den Titel

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
                Column(modifier = Modifier.fillMaxSize()) {
                    // Eingabezeile für neuen Bucket
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newBucketTitle.value,
                            onValueChange = { newBucketTitle.value = it },
                            label = { Text("Bucket Title") },
                            modifier = Modifier.weight(1f)
                        )

                        Button(onClick = {
                            if (newBucketTitle.value.isNotBlank()) {
                                viewModel.addBucketEntry(
                                    title = newBucketTitle.value,
                                    description = null, // Keine Beschreibung nötig
                                    priority = 0,
                                    icon = null,
                                    latitude = 0.0,
                                    longitude = 0.0,
                                    timestamp = System.currentTimeMillis()
                                )
                                newBucketTitle.value = "" // Zurücksetzen des Textfelds
                                bucketList = viewModel.getAllBucketEntriesAsc() // Update the bucket list
                            }
                        }) {
                            Text("Add")
                        }
                    }
                    if (bucketList.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No Goals Set.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        // Liste der Buckets
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(PaddingValues(16.dp, 0.dp, 16.dp, 8.dp)),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(bucketList) { e ->
                                var openAlertDialog by remember { mutableStateOf(false)}
                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth().clickable {
                                            rootNavController.currentBackStackEntry?.savedStateHandle?.apply {
                                                set("itemId", e.id)
                                            }
                                            rootNavController.navigate(Routes.ItemDetail.route)},
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
                                        ) {
                                            Text(
                                                text = e.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Icon",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clickable {
                                                    openAlertDialog = true
                                                }
                                        )
                                    }
                                }

                                if (openAlertDialog) {
                                    AlertDialog(
                                        icon = {
                                            Icon(Icons.Default.Delete, contentDescription = "Example Icon")
                                        },
                                        title = {
                                            Text(text = "Delete")
                                        },
                                        text = {
                                            Text(text = "Are you sure you want to delete this Item?")
                                        },
                                        onDismissRequest = {
                                            openAlertDialog = false
                                        },
                                        confirmButton = {
                                            TextButton(
                                                onClick = {
                                                    openAlertDialog = false
                                                    viewModel.deleteBucketItem(e.id)
                                                    bucketList = viewModel.getAllBucketEntriesAsc() // Update the bucket list
                                                }
                                            ) {
                                                Text("Confirm")
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(
                                                onClick = {
                                                    openAlertDialog = false
                                                }
                                            ) {
                                                Text("Dismiss")
                                            }
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
