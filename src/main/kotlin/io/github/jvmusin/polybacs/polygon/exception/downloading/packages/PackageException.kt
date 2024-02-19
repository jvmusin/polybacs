package io.github.jvmusin.polybacs.polygon.exception.downloading.packages

import io.github.jvmusin.polybacs.polygon.exception.downloading.ProblemDownloadingException

/**
 * Package exception.
 *
 * Thrown if something is wrong with a problem package.
 */
abstract class PackageException(message: String) : ProblemDownloadingException(message)
