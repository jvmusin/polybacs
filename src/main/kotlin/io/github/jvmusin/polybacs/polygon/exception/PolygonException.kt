package io.github.jvmusin.polybacs.polygon.exception

import io.github.jvmusin.polybacs.ConverterException

/**
 * Polygon exception.
 *
 * Thrown if something bad happened to Polygon.
 */
abstract class PolygonException(message: String, cause: Throwable? = null) : ConverterException(message, cause)
