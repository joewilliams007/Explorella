package com.app.explorella.overpass

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val OVERPASS_API_URL = "https://overpass-api.de/api/interpreter"
private const val OVERPASS_DEBUG = false;

/**
 * Query Overpass api by name.
 *
 * Example usage:
 *
 *         LaunchedEffect(Unit) {
 *             Overpass().searchLocations("Mannheim")
 *         }
 */
class Overpass {
    /**
     * Searches the Overpass API with the given search string and returns a list of locations.
     */
    suspend fun searchLocations(searchTerm: String): List<Element> {
        val query = buildOverpassQuery(searchTerm)
        val response = fetchLocations(query)

        if (OVERPASS_DEBUG) {
            response.elements.forEach {
                println("Name: ${it.tags["name"]}, Lat: ${it.lat}, Lon: ${it.lon}")
            }
        }

        return response.elements
    }

    @Serializable
    data class OverpassResponse(val elements: List<Element>)

    @Serializable
    data class Element(val id: Long, val lat: Double, val lon: Double, val tags: Map<String, String>)

    /**
     * Request locations from the Overpass API.
     */
    private suspend fun fetchLocations(query: String): OverpassResponse {
        val client = HttpClient()

        return client.use {
            val response: String = client.get(OVERPASS_API_URL) {
                parameter("data", query)
            }.bodyAsText()

            if (OVERPASS_DEBUG) {
                println("Params: $query")
                println("Raw Response: $response")
            }

            val json = Json {
                ignoreUnknownKeys = true
            }
            json.decodeFromString(OverpassResponse.serializer(), response)
        }
    }

    /**
     * Configure the query parameters.
     */
    private fun buildOverpassQuery(searchTerm: String): String {
        return """
        [out:json];
        node["name"~"$searchTerm"];
        out;
    """.trimIndent()
    }
}