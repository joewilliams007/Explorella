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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import app.cash.sqldelight.db.SqlDriver
import com.app.explorella.BucketItem
import com.app.explorella.database.BucketViewModel
import com.app.explorella.navigation.Routes
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
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
                    text = "Timeline",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        )
        Spacer(modifier = Modifier.height(20.dp))

        DisplayPage(rootNavController, sqlDriver)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayPage(rootNavController: NavController, sqlDriver: SqlDriver) {
    val bucket = remember { BucketViewModel(sqlDriver = sqlDriver) }
    val bucketEntries = remember { mutableStateOf(emptyList<BucketItem>()) }
    val isAscending = rememberSaveable { mutableStateOf(true) }

    // Fetch and sort data
    LaunchedEffect(Unit) {
        updateCompleteBucketEntries(bucket, bucketEntries, isAscending)
    }

    // Group entries by Year
    val groupedEntries = remember(bucketEntries.value) {
        bucketEntries.value
            .groupBy { getYearFromTimestamp(it.timestamp!!) }
    }

    if (bucketEntries.value.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No Items Completed.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            // Sorting Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        isAscending.value = !isAscending.value
                        updateCompleteBucketEntries(bucket, bucketEntries, isAscending)
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (isAscending.value) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Sort Order",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isAscending.value) "Asc" else "Desc",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Timeline Layout
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Iterate over the grouped entries
                groupedEntries.forEach { (year, entries) ->
                    // Year Header
                    item {
                        Text(
                            text = "$year",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Display entries for this year
                    items(entries) { entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Timeline Point",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            TimeCard(rootNavController, entry, bucket, bucketEntries, isAscending)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeCard(
    rootNavController: NavController,
    entry: BucketItem,
    bucket: BucketViewModel,
    bucketEntries: MutableState<List<BucketItem>>,
    isAscending: MutableState<Boolean>
) {
    // Card Content
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
        onClick = {
            rootNavController.currentBackStackEntry?.savedStateHandle?.apply {
                set("itemId", entry.id)
            }
            rootNavController.navigate(Routes.ItemDetail.route)
        },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatTimestamp(entry.timestamp!!),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Dropdown Menu for More Options
            DropDownMenu(rootNavController, entry.id, bucket, bucketEntries, isAscending)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownMenu(
    rootNavController: NavController,
    id: Long,
    bucket: BucketViewModel,
    bucketEntries: MutableState<List<BucketItem>>,
    isAscending: MutableState<Boolean>,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var openAlertDialog by remember { mutableStateOf(false) }

    IconButton(
        onClick = {
            expanded = true
        },

        ) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More Options",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = { Text("Mark Incomplete") },
            onClick = {
                expanded = false
                bucket.setBucketItemComplete(0, id)
                updateCompleteBucketEntries(bucket, bucketEntries, isAscending)
            }
        )
        DropdownMenuItem(
            text = { Text("Delete") },
            onClick = {
                expanded = false
                openAlertDialog = true
            }
        )
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
                        bucket.deleteBucketItem(id)
                        updateCompleteBucketEntries(bucket, bucketEntries, isAscending)
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
fun updateCompleteBucketEntries(
    bucket: BucketViewModel,
    bucketEntries: MutableState<List<BucketItem>>,
    isAscending: MutableState<Boolean>,
    ) {
    bucketEntries.value = bucket.getCompleteBucketEntries()
        .sortedBy { if (isAscending.value) it.timestamp else -it.timestamp!! }
}

// Helper function to get the year from the timestamp
fun getYearFromTimestamp(timestamp: Long): Int {
    val formatter = DateTimeFormatter.ofPattern("yyyy")
        .withZone(ZoneId.systemDefault())
    return formatter.format(Instant.ofEpochMilli(timestamp)).toInt()
}

// Timestamp Formatter
fun formatTimestamp(timestamp: Long): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        .withZone(ZoneId.systemDefault())
    return formatter.format(Instant.ofEpochMilli(timestamp))
}
