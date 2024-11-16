package com.app.explorella.database

import androidx.lifecycle.ViewModel
import app.cash.sqldelight.db.SqlDriver
import com.app.explorella.createDatabase

class PreferencesViewModel(sqlDriver: SqlDriver) : ViewModel() {
    private val database = createDatabase(sqlDriver)
    private val queries = database.databaseQueries

    companion object {
        const val MAP_LOCATION_LATITUDE = "map_location_latitude"
        const val MAP_LOCATION_LATITUDE_DEFAULT = "49.47390194794475"

        const val MAP_LOCATION_LONGITUDE = "map_location_longitude"
        const val MAP_LOCATION_LONGITUDE_DEFAULT = "8.53483734185281"

        const val MAP_ZOOM_LEVEL = "map_zoom_level"
        const val MAP_ZOOM_LEVEL_DEFAULT = "5"
    }

    /**
     * Set a preference value.
     */
    fun setPreference(preferenceKey: String, preferenceValue: String) {
        database.transaction {
            val existingPreference = queries.getPreference(preferenceKey).executeAsOneOrNull()

            if (existingPreference != null) {
                queries.setPreference(preference_value = preferenceValue, preference_key = preferenceKey)
            } else {
                queries.createPreference(preference_key = preferenceKey, preference_value = preferenceValue)
            }
        }
    }

    /**
     * Get a preference. Returns default value if preference does not exist.
     */
    fun getPreference(preferenceKey: String, default: String): String {
        return try {
            queries.getPreference(preferenceKey).executeAsOne().preference_value
        } catch (e: Exception) {
            default
        }
    }
}