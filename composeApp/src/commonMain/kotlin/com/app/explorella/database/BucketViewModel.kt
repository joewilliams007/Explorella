package com.app.explorella.database

import androidx.lifecycle.ViewModel
import app.cash.sqldelight.db.SqlDriver
import com.app.explorella.BucketItem
import com.app.explorella.createDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BucketViewModel(sqlDriver: SqlDriver) : ViewModel() {
    private val database = createDatabase(sqlDriver)
    private val bucketQueries = database.databaseQueries
    private val _bucketEntries = MutableStateFlow<List<BucketItem>>(emptyList())
    val bucketEntries: StateFlow<List<BucketItem>> = _bucketEntries

    // Function to insert a new bucket entry
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
        getAllBucketEntries()
    }

    // Function to get all bucket entries
    fun getAllBucketEntries(): List<BucketItem> {
        val items =  bucketQueries.selectAllBucketItemsDesc().executeAsList()
        _bucketEntries.value = items
        return items
    }
}