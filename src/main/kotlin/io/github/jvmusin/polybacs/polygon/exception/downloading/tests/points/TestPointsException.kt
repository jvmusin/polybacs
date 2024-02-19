package io.github.jvmusin.polybacs.polygon.exception.downloading.tests.points

import io.github.jvmusin.polybacs.polygon.exception.downloading.tests.TestsException

/**
 * Test points exception.
 *
 * Thrown if something bad happened to test points.
 */
abstract class TestPointsException(message: String) : TestsException(message)
