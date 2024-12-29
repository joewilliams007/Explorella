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
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

@Composable
fun DisplayPage(rootNavController: NavController, sqlDriver: SqlDriver) {
    val bucket = remember { BucketViewModel(sqlDriver = sqlDriver) }
    val bucketEntries = remember { mutableStateOf(emptyList<BucketItem>()) }
    val isAscending = rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        bucketEntries.value = bucket.getIncompleteBucketEntries()
            .sortedBy { if (isAscending.value) it.timestamp else -it.timestamp!! }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sorting Button
        IconButton(
            onClick = {
                isAscending.value = !isAscending.value
                bucketEntries.value = bucketEntries.value
                    .sortedBy { if (isAscending.value) it.timestamp else -it.timestamp!! }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = if (isAscending.value) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Sort Order",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // LazyColumn for Items
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(bucketEntries.value) { entry ->
                // Individual Item Card
                androidx.compose.material3.Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Navigate to detail screen with the item ID
                            rootNavController.currentBackStackEntry?.savedStateHandle?.apply {
                                set("itemId", entry.id)
                            }
                            rootNavController.navigate(Routes.ItemDetail.route)
                        },
                    elevation = androidx.compose.material3.CardDefaults.cardElevation(4.dp) // Fix applied here
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Timeline Icon
                        Icon(
                            imageVector = Icons.Default.CheckCircle, // Replace with a timeline-related icon
                            contentDescription = "Timeline Icon",
                            modifier = Modifier
                                .size(32.dp)
                                .padding(end = 16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )

                        // Title and Timestamp
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

                        // More Options Icon
                        IconButton(
                            onClick = {
                                rootNavController.currentBackStackEntry?.savedStateHandle?.apply {
                                    set("itemId", entry.id)
                                }
                                rootNavController.navigate(Routes.ItemDetail.route)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
fun formatTimestamp(timestamp: Long): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        .withZone(ZoneId.systemDefault())
    return formatter.format(Instant.ofEpochMilli(timestamp))
}