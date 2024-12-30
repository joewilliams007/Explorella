package com.app.explorella

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val dbPath = File("database.db").absolutePath
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:$dbPath")

        if (!File(dbPath).exists()) {
            Database.Schema.create(driver)
        }
        return driver
    }
}