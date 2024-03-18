package io.github.jvmusin.polybacs.polygon

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

/**
 * Polygon API configuration properties.
 *
 * @property apiKey apiKey to access the API.
 * @property secret secret key to access the API.
 */
@Configuration
data class PolygonConfig(
    @Value("\${polygon.apiKey}") val apiKey: String,
    @Value("\${polygon.secret}") val secret: String,
)
