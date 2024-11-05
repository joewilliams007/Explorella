package com.app.explorella.database

import app.cash.sqldelight.db.SqlDriver
import com.app.explorella.Database

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): Database {
    val driver = driverFactory.createDriver()
    val database = Database(driver)
    return database
}
