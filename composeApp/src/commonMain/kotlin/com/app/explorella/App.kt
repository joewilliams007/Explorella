package com.app.explorella

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import app.cash.sqldelight.db.SqlDriver
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(sqlDriver: SqlDriver) {
    MaterialTheme {
        MainScreen(sqlDriver)
    }
}