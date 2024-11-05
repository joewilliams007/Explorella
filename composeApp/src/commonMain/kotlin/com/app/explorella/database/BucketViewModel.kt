package com.app.explorella.database

import androidx.lifecycle.ViewModel
import com.app.explorella.BucketEntry

class BucketViewModel(driverFactory: DriverFactory) : ViewModel() {
    private val database = createDatabase(driverFactory)
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