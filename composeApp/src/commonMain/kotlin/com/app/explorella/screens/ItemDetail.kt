package com.app.explorella.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable

import androidx.navigation.NavController
import app.cash.sqldelight.db.SqlDriver
import com.app.explorella.BucketItem
import com.app.explorella.database.BucketViewModel

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

        // Beschreibung
        OutlinedTextField(
            value = localDescription,
            onValueChange = { localDescription = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 1,
            maxLines = 10
        )

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
            Text("Update Description")
        }

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
                    },
                    onCheckedChange = { isChecked ->
                        // TODO: Implement checkbox state management
                    }
                )
            }
        }
    }
}

@Composable
fun TodoItemCard(task: String, onDelete: () -> Unit, onCheckedChange: (Boolean) -> Unit) {
    var isChecked by remember { mutableStateOf(false) }

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
                onCheckedChange = {
                    isChecked = it
                    onCheckedChange(it)
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
