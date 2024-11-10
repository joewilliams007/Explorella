package com.app.explorella

import androidx.lifecycle.ViewModel
import app.cash.sqldelight.db.SqlDriver

class BucketViewModel(sqlDriver: SqlDriver) : ViewModel() {
    private val database = createDatabase(sqlDriver)
    private val bucketQueries = database.databaseQueries

    // Function to insert a new bucket entry
    fun addBucketEntry(title: String, description: String?, latitude: Double, longitude: Double) {
        bucketQueries.insertBucketEntry(
            title = title,
            description = description,
            latitude = latitude,
            longitude = longitude
        )
    }

    // Function to get all bucket entries
    fun getAllBucketEntries(): List<BucketEntry> {
        return bucketQueries.selectAllBucketEntries().executeAsList()
    }
}