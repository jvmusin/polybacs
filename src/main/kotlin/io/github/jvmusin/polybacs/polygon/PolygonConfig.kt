package io.github.jvmusin.polybacs.polygon

/**
 * Polygon API configuration properties.
 *
 * @property apiKey apiKey to access the API.
 * @property secret secret key to access the API.
 */
data class PolygonConfig(
    val apiKey: String,
    val secret: String
)
