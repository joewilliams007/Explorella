package com.app.explorella

import androidx.compose.runtime.Composable
import app.cash.sqldelight.db.SqlDriver
import com.app.explorella.ui.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(sqlDriver: SqlDriver) {
    AppTheme {
        MainScreen(sqlDriver)
    }
}