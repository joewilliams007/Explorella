package com.app.explorella.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
    val viewModel: BucketViewModel = BucketViewModel(
        sqlDriver = sqlDriver
    )

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


    FloatingActionButton(
        onClick = {
            TODO("navigate to add buckets. add 'add buckets' as a route")
        }
    ) {
        Icon(Icons.Filled.Add, "Add Bucket")
    }

    LazyColumn (
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(viewModel.getAllBucketEntriesAsc()) { e ->
            Button(
                onClick = {
                    rootNavController.navigate(Routes.ItemDetail.route)
                    TODO("navigate to ItemDetail")
                }
            ) {
                Text(e.title)
            }
        }
    }
}