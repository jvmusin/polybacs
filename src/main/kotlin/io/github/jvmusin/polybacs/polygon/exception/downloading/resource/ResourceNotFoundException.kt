package io.github.jvmusin.polybacs.polygon.exception.downloading.resource

import io.github.jvmusin.polybacs.polygon.exception.downloading.ProblemDownloadingException

/**
 * Resource not found exception.
 *
 * Thrown if some required problem resource is missing.
 */
abstract class ResourceNotFoundException(message: String) : ProblemDownloadingException(message)
