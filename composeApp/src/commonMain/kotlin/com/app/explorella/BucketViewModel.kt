package com.app.explorella

import androidx.lifecycle.ViewModel
import app.cash.sqldelight.db.SqlDriver

class BucketViewModel(sqlDriver: SqlDriver) : ViewModel() {
    private val database = createDatabase(sqlDriver)
    private val bucketQueries = database.databaseQueries

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
    }

    // Function to get all bucket entries
    fun getAllBucketEntries(): List<BucketItem> {
        return bucketQueries.selectAllBucketItemsDesc().executeAsList()
    }
}