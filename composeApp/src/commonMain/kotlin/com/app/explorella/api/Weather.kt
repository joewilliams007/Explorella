package com.app.explorella.api

import com.app.explorella.screens.Coordinate
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val API_URL = "https://api.open-meteo.com/v1/forecast"
private const val API_DEBUG = false;

/**
 * Query Overpass api by name.
 *
 * Example usage:
 *
 *         LaunchedEffect(Unit) {
 *             Weather().getWeather(Coordinate)
 *         }
 */
class Weather {
    /**
     * Searches the Weather API with the given coordinates.
     */
    suspend fun getWeather(coordinate: Coordinate): Double {
        val response = fetchCurrentWeather(coordinate)

        if (API_DEBUG) {
            println("Temperature: $response")
        }

        return response
    }

    @Serializable
    data class CurrentWeatherResponse(
        val current_weather: WeatherData
    )

    @Serializable
    data class WeatherData(
        val temperature: Double // Only extracting the temperature value
    )
    /**
     * Request locations from the Overpass API.
     */
    private suspend fun fetchCurrentWeather(coordinate: Coordinate): Double {
        val client = HttpClient()

        return client.use {
            val response: String = client.get(API_URL) {
                parameter("latitude", coordinate.latitude)
                parameter("longitude", coordinate.longitude)
                parameter("current_weather", true)
            }.bodyAsText()

            if (API_DEBUG) {
                println("Latitude: ${coordinate.latitude}, Longitude: ${coordinate.longitude}")
                println("Raw Response: $response")
            }

            val json = Json {
                ignoreUnknownKeys = true
            }

            val currentWeatherResponse = json.decodeFromString(CurrentWeatherResponse.serializer(), response)
            currentWeatherResponse.current_weather.temperature
        }
    }
}