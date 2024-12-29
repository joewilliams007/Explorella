package com.app.explorella.database

import androidx.lifecycle.ViewModel
import app.cash.sqldelight.db.SqlDriver
import com.app.explorella.BucketItem
import com.app.explorella.Todo
import com.app.explorella.createDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BucketViewModel(sqlDriver: SqlDriver) : ViewModel() {
    private val database = createDatabase(sqlDriver)
    private val bucketQueries = database.databaseQueries
    private val _bucketEntries = MutableStateFlow<List<BucketItem>>(emptyList())
    val bucketEntries: StateFlow<List<BucketItem>> = _bucketEntries

    private val _todoItems = MutableStateFlow<List<Todo>>(emptyList())
    val todoItems: StateFlow<List<Todo>> = _todoItems


    /**
     * Insert a new bucket entry.
     */
    fun addBucketEntry(title: String, description: String?, priority: Long, icon: String?, latitude: Double, longitude: Double, timestamp: Long) {
        bucketQueries.insertBucketItem(
            title = title,
            description = description,
            priority = priority,
            icon = icon,
            latitude = latitude,
            longitude = longitude,
            timestamp = timestamp
        )
        getAllBucketEntriesDesc()
    }

    private var currentBucketId: Long = -1L

    fun currentBucket(bucketId: Long) {
        currentBucketId = bucketId
        val todos = bucketQueries.selectTodosByBucket(bucketId).executeAsList()
        _todoItems.value = todos
    }

    /** NEW !
     * Update an existing bucket entry by id.
     * Make sure you have a matching SQL statement in your .sq file (e.g. 'updateBucketItem').
     */
    fun updateBucketEntry(
        id: Long,
        title: String,
        description: String?,
        priority: Long,
        icon: String?,
        latitude: Double,
        longitude: Double,
        complete: Long,             // falls du ein entsprechendes Feld hast
        timestamp: Long
    ) {
        bucketQueries.updateBucketItem(
            title = title,
            description = description,
            priority = priority,
            icon = icon,
            latitude = latitude,
            longitude = longitude,
            complete = complete,  // anpassen, falls deine Tabelle dieses Feld hat
            timestamp = timestamp,
            id = id
        )
        // Anschließend Liste neu laden
        getAllBucketEntriesDesc()
    }

    /**
     * Get bucket entry.
     */
    fun getBucketEntry(id: Long): BucketItem {
        return bucketQueries.selectBucketItem(id).executeAsOne()
    }

    /**
     * Get all bucket entries descending order.
     */
    fun getAllBucketEntriesDesc(): List<BucketItem> {
        val items =  bucketQueries.selectAllBucketItemsDesc().executeAsList()
        _bucketEntries.value = items
        return items
    }

    /**
     * Get all bucket entries ascending order.
     */
    fun getAllBucketEntriesAsc(): List<BucketItem> {
        val items =  bucketQueries.selectAllBucketItemsAsc().executeAsList()
        _bucketEntries.value = items
        return items
    }

    /**
     * Get complete bucket entries.
     */
    fun getCompleteBucketEntries(): List<BucketItem> {
        val items =  bucketQueries.selectCompleteBucketItems().executeAsList()
        _bucketEntries.value = items
        return items
    }

    /**
     * Get incomplete bucket entries.
     */
    fun getIncompleteBucketEntries(): List<BucketItem> {
        val items =  bucketQueries.selectIncompleteBucketItems().executeAsList()
        _bucketEntries.value = items
        return items
    }

    // -----------------------------------------------------------
    // TODO-Funktionen
    // -----------------------------------------------------------

    /**
     * Lädt alle Todos für ein bestimmtes BucketItem.
     * Hier wird dein 'selectTodosByBucket' ausgeführt, das du
     * in der .sq definiert hast.
     */
    fun loadTodosForBucket(bucketId: Long) {
        println("Loading todos for bucket_id: $bucketId")
        val result = bucketQueries.selectTodosByBucket(bucketId).executeAsList()
        println("Loaded Todos: $result")
        _todoItems.value = result
    }


    /**
     * Legt ein neues Todo an:
     *  - [task]: Text der Aufgabe
     *  - [bucketId]: Zu welchem BucketItem gehört diese Aufgabe?
     */
    fun addTodo(task: String, bucketId: Long) {
        println("Adding Todo: $task to bucketId: $bucketId")
        bucketQueries.insertTodo(
            task = task,
            bucket_id = bucketId,
            timestamp = System.currentTimeMillis()
        )
        loadTodosForBucket(bucketId)
    }

    /**
     * Setzt das 'complete'-Feld eines Todos (abgehakt/erledigt).
     *  - [complete]: true/false => 1/0 in der DB
     */
    fun setTodoComplete(todoId: Long, complete: Boolean, bucketId: Long) {
        bucketQueries.setCompleteTodo(
            complete = if (complete) 1 else 0,
            id = todoId
        )
        loadTodosForBucket(bucketId)
    }

    /**
     * Ändert den 'task'-Text eines bestehenden Todos
     */
    fun setTodoTask(todoId: Long, newTask: String, bucketId: Long) {
        bucketQueries.setTaskTodo(
            task = newTask,
            id = todoId
        )
        loadTodosForBucket(bucketId)
    }

    fun deleteTodo(id: Long) {
        bucketQueries.deleteTodo(id)
        // Liste der Todos nach dem Löschen aktualisieren
        loadTodosForBucket(currentBucketId) // `currentBucketId` sollte der aktuell geladene Bucket sein
    }

    fun setBucketComplete(id: Long, complete: Boolean) {
        bucketQueries.setBucketComplete(if (complete) 1 else 0, id)
        // Optional: Lade die Bucket-Einträge erneut, falls nötig
    }



}