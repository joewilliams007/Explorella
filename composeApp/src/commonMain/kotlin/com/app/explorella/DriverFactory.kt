package com.app.explorella

import app.cash.sqldelight.db.SqlDriver

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(sqlDriver: SqlDriver): Database {
    val database = Database(sqlDriver)
    return database
}
