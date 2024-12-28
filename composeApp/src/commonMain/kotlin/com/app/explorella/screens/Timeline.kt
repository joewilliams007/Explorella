package com.app.explorella.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import app.cash.sqldelight.db.SqlDriver
import com.app.explorella.BucketItem
import com.app.explorella.database.BucketViewModel

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

        DisplayPage(sqlDriver)
    }
}

@Composable
fun DisplayPage(sqlDriver: SqlDriver) {
    val bucket = remember { BucketViewModel(sqlDriver = sqlDriver) }
    val bucketEntries = remember { mutableStateOf(emptyList<BucketItem>()) }
    val isAscending = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        bucketEntries.value = bucket.getIncompleteBucketEntries()
            .sortedBy { if (isAscending.value) it.timestamp else -it.timestamp!! }
    }

    Column {
        Text(
            text = if (isAscending.value) "Sort: Ascending" else "Sort: Descending",
            modifier = Modifier
                .padding(16.dp)
                .clickable {
                    isAscending.value = !isAscending.value
                    bucketEntries.value = bucketEntries.value
                        .sortedBy { if (isAscending.value) it.timestamp else -it.timestamp!! }
                }
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(bucketEntries.value) { entry ->
                Text(
                    text = entry.title,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
