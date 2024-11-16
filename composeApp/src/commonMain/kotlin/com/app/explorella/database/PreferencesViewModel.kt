package com.app.explorella.database

import androidx.lifecycle.ViewModel
import app.cash.sqldelight.db.SqlDriver
import com.app.explorella.Preferences
import com.app.explorella.createDatabase

class PreferencesViewModel(sqlDriver: SqlDriver) : ViewModel() {
    private val database = createDatabase(sqlDriver)
    private val queries = database.databaseQueries

    /**
     * Set a preference value.
     */
    fun setPreference(preferenceKey: String, preferenceValue: String) {
        database.transaction {
            val existingPreference = queries.getPreference(preferenceKey).executeAsOneOrNull()

            if (existingPreference != null) {
                queries.setPreference(preference_value = preferenceKey, preference_key = preferenceValue)
            } else {
                queries.createPreference(preference_key = preferenceKey, preference_value = preferenceValue)
            }
        }
    }

    /**
     * Get a preference.
     */
    fun getPreference(preferenceKey: String): Preferences {
        return queries.getPreference(preferenceKey).executeAsOne()
    }
}