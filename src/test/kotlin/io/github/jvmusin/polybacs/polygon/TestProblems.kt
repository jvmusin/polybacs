package io.github.jvmusin.polybacs.polygon

object TestProblems {
    const val noBuiltPackagesProblem = 159635
    const val problemWithOnlyReadAccess = 69927
    const val modifiedProblem = 157878
    const val totallyUnknownProblem = 157877
    const val oldPackageProblem = 157883
    const val interactiveProblem = 159640
    const val problemWithoutStatement = 155265
    const val problemWithoutPdfStatement = 159743
    const val problemWithoutCppChecker = 159744

    const val problemWithTestGroups = 159528
    const val problemWithTestGroupsExceptSamples = 159558

    /**
     * Problem with non sequential test indices.
     *
     * It contains tests with indices **1** and **3**.
     *
     * Polygon does not allow building a package when tests are enumerated incorrectly,
     * so we can not check this exception using Polygon directly.
     *
     * Probably, there is some other way to do it.
     */
    const val problemWithNonSequentialTestIndices = 160331

    /**
     * Problem that has two tests, where second is a sample and first is not.
     */
    const val problemWhereSampleGoesSecond = 160334

    /**
     * Problem that has three tests, where first and third are samples and second is not.
     */
    const val problemWhereSamplesAreFirstAndThirdTests = 160354

    /**
     * Problem with missing test groups.
     *
     * It has three tests, first is sample.
     * First two tests are in groups **'samples', 'second'**,
     * and the third one does not have a group.
     */
    const val problemWithMissingTestGroups = 160340

    /**
     * Problem that has 4 tests and groups **'samples' 'first', 'second', 'first'**.
     */
    const val problemWithNonSequentialTestsInTestGroup = 160342

    /**
     * Problem that has 3 tests, first two are samples, ang groups **'first', 'second', 'second'**.
     */
    const val problemWhereSamplesAreNotFormingFirstTestGroup = 160344

    /**
     * Problem that has 10 points on sample and no other tests.
     */
    const val problemWithPointsOnSample = 160360

    /**
     * Problem that has 10 points on samples group and no other tests except this one.
     */
    const val problemWithPointsOnSamplesGroup = 160362

    /**
     * Problem with normal test groups and points.
     *
     * Has 6 tests:
     * * 1 - samples - no points
     * * 2 - first - no points
     * * 3 - first - 10 points
     * * 4 - second - 5 points
     * * 5 - second - 5 points
     * * 6 - third - no points
     *
     * And points policies:
     * * first - COMPLETE_GROUP
     * * samples, second, third - EACH_TEST
     *
     * Test inputs are **numbers of tests** and answers are **ans{number of test}**.
     */
    const val problemWithNormalTestGroupsAndPoints = 160358

    /**
     * Problem with double test points
     *
     * It has two tests: sample and a test with score of **1.5**.
     */
    const val problemWithNonIntegralTestPoints = 160371

    /**
     * Problem with test groups but no points enabled
     *
     * Test points are disabled.
     *
     * Has two tests - first test is a sample with group **'sample'**, second test has group **'first'**.
     */
    const val problemWithTestGroupsButNoPointsEnabled = 160373

    /**
     * Problem with two solutions - `main.cpp` and `wa.cpp`.
     */
    const val problemWithTwoSolutions = 357044
}
