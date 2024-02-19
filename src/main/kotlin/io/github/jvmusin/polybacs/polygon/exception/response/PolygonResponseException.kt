package io.github.jvmusin.polybacs.polygon.exception.response

import io.github.jvmusin.polybacs.polygon.api.PolygonResponse
import io.github.jvmusin.polybacs.polygon.exception.PolygonException

/**
 * PolygonResponse result extracting exception.
 *
 * Thrown if [PolygonResponse.result] is *null*.
 *
 * @param comment message taken from [PolygonResponse.comment].
 */
open class PolygonResponseException(comment: String) : PolygonException(comment)
