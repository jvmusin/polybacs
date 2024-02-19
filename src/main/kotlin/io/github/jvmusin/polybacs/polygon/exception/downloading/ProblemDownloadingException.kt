package io.github.jvmusin.polybacs.polygon.exception.downloading

import io.github.jvmusin.polybacs.polygon.exception.PolygonException

/**
 * Problem downloading exception.
 *
 * Thrown if some error occurred while downloading the problem.
 */
open class ProblemDownloadingException(message: String, cause: Throwable? = null) : PolygonException(message, cause)
