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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import app.cash.sqldelight.db.SqlDriver
import com.app.explorella.database.BucketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBucketScreen(
    rootNavController: NavController,
    paddingValues: PaddingValues,
    sqlDriver: SqlDriver
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("49.474358383071454") }
    var longitude by remember { mutableStateOf("8.534289721213689") }

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

        // Input fields
        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") }
        )
        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") }
        )
        TextField(
            value = latitude,
            onValueChange = { latitude = it },
            label = { Text("Latitude") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        TextField(
            value = longitude,
            onValueChange = { longitude = it },
            label = { Text("Longitude") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        /**
         * This is only an example on how to use the database!!
         *
         * To create the structure of a new table please enter the queries to the file
         * /AndroidStudioProjects/Explorella/composeApp/src/commonMain/sqldelight/com/app/explorella/Database.sq
         *
         * The command ./gradlew :composeApp:generateSqlDelightInterface will then generate the type-safe code for use.
         * For further abstraction, create a viewmodel. Such as e.g. BucketViewModel.
         */
        val viewModel: BucketViewModel = BucketViewModel(
            sqlDriver = sqlDriver
        )

        // Button to add a bucket entry
        Button(onClick = {
            viewModel.addBucketEntry(
                title = title,
                description = description,
                priority = 0,
                icon = null,
                latitude = latitude.toDoubleOrNull() ?: 0.0,
                longitude = longitude.toDoubleOrNull() ?: 0.0,
                timestamp = System.currentTimeMillis()
            )
        }) {
            Text("Add Bucket Entry")
        }

        val bucketEntries by viewModel.bucketEntries.collectAsState()

        LazyColumn {
            items(bucketEntries) { entry ->
                Text(text = entry.title)
            }
        }
    }
}