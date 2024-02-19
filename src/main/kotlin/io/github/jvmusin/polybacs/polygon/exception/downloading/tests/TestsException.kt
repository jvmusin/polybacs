package io.github.jvmusin.polybacs.polygon.exception.downloading.tests

import io.github.jvmusin.polybacs.polygon.exception.downloading.ProblemDownloadingException

/**
 * Tests exception.
 *
 * Thrown if something bad happened to tests of the problem.
 */
abstract class TestsException(message: String) : ProblemDownloadingException(message)
