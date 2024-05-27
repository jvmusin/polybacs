package io.github.jvmusin.polybacs.polygon

import io.github.jvmusin.polybacs.api.StatementFormat
import io.github.jvmusin.polybacs.ir.IRProblem
import io.github.jvmusin.polybacs.ir.IRTest
import io.github.jvmusin.polybacs.ir.IRTestGroup
import io.github.jvmusin.polybacs.ir.IRTestGroupPointsPolicy
import io.github.jvmusin.polybacs.polygon.TestProblems.interactiveProblem
import io.github.jvmusin.polybacs.polygon.TestProblems.modifiedProblem
import io.github.jvmusin.polybacs.polygon.TestProblems.noBuiltPackagesProblem
import io.github.jvmusin.polybacs.polygon.TestProblems.oldPackageProblem
import io.github.jvmusin.polybacs.polygon.TestProblems.problemWhereSampleGoesSecond
import io.github.jvmusin.polybacs.polygon.TestProblems.problemWhereSamplesAreFirstAndThirdTests
import io.github.jvmusin.polybacs.polygon.TestProblems.problemWhereSamplesAreNotFormingFirstTestGroup
import io.github.jvmusin.polybacs.polygon.TestProblems.problemWithMissingTestGroups
import io.github.jvmusin.polybacs.polygon.TestProblems.problemWithNonIntegralTestPoints
import io.github.jvmusin.polybacs.polygon.TestProblems.problemWithNonSequentialTestIndices
import io.github.jvmusin.polybacs.polygon.TestProblems.problemWithNonSequentialTestsInTestGroup
import io.github.jvmusin.polybacs.polygon.TestProblems.problemWithNormalTestGroupsAndPoints
import io.github.jvmusin.polybacs.polygon.TestProblems.problemWithPointsOnSample
import io.github.jvmusin.polybacs.polygon.TestProblems.problemWithPointsOnSamplesGroup
import io.github.jvmusin.polybacs.polygon.TestProblems.problemWithTestGroupsButNoPointsEnabled
import io.github.jvmusin.polybacs.polygon.TestProblems.problemWithoutCppChecker
import io.github.jvmusin.polybacs.polygon.TestProblems.problemWithoutPdfStatement
import io.github.jvmusin.polybacs.polygon.TestProblems.problemWithoutStatement
import io.github.jvmusin.polybacs.polygon.TestProblems.totallyUnknownProblem
import io.github.jvmusin.polybacs.polygon.exception.downloading.ProblemDownloadingException
import io.github.jvmusin.polybacs.polygon.exception.downloading.format.ProblemModifiedException
import io.github.jvmusin.polybacs.polygon.exception.downloading.format.UnsupportedFormatException
import io.github.jvmusin.polybacs.polygon.exception.downloading.packages.NoPackagesBuiltException
import io.github.jvmusin.polybacs.polygon.exception.downloading.packages.OldBuiltPackageException
import io.github.jvmusin.polybacs.polygon.exception.downloading.resource.CheckerNotFoundException
import io.github.jvmusin.polybacs.polygon.exception.downloading.resource.StatementNotFoundException
import io.github.jvmusin.polybacs.polygon.exception.downloading.tests.*
import io.github.jvmusin.polybacs.polygon.exception.downloading.tests.points.NonIntegralTestPointsException
import io.github.jvmusin.polybacs.polygon.exception.downloading.tests.points.PointsOnSampleException
import io.github.jvmusin.polybacs.polygon.exception.downloading.tests.points.TestPointsDisabledException
import io.github.jvmusin.polybacs.polygon.exception.response.NoSuchProblemException
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PolygonProblemDownloaderTests(private val downloader: PolygonProblemDownloader) : BehaviorSpec() {
    private suspend inline fun <reified TException : Throwable> downloadProblemWithException(
        problemId: Int,
        includeTests: Boolean = false,
    ) {
        shouldThrowExactly<TException> {
            downloader.downloadProblem(
                problemId,
                includeTests,
                statementFormat = StatementFormat.PDF,
                language = "russian"
            )
        }.shouldBeInstanceOf<ProblemDownloadingException>()
    }

    private suspend fun PolygonProblemDownloader.downloadProblem(problemId: Int, includeTests: Boolean): IRProblem {
        return downloadProblem(problemId, includeTests, StatementFormat.PDF, "russian")
    }

    @Suppress("TestFunctionName")
    private fun IRTest(
        index: Int,
        isSample: Boolean,
        input: String,
        output: String,
        points: Int?,
        groupName: String?,
    ) = IRTest(index, isSample, input.toByteArray(), output.toByteArray(), points, groupName)

    init {
        @Suppress("LeakingThis") // OK for tests
        Given("downloadProblem") {
            When("problem is unknown") {
                Then("throws NoSuchProblemException") {
                    shouldThrowExactly<NoSuchProblemException> {
                        downloader.downloadProblem(totallyUnknownProblem, false)
                    }
                }
            }
            When("problem is modified") {
                Then("throws ProblemModifiedException") {
                    downloadProblemWithException<ProblemModifiedException>(modifiedProblem)
                }
            }
            When("no packages build") {
                Then("throws NoPackagesBuiltException") {
                    downloadProblemWithException<NoPackagesBuiltException>(noBuiltPackagesProblem)
                }
            }
            When("last built package is old") {
                Then("throws OldBuiltPackageException") {
                    downloadProblemWithException<OldBuiltPackageException>(oldPackageProblem)
                }
            }
            When("problem is interactive") {
                Then("throws UnsupportedFormatException") {
                    downloadProblemWithException<UnsupportedFormatException>(interactiveProblem)
                }
            }
            When("problem has no statement") {
                Then("throws StatementNotFoundException") {
                    downloadProblemWithException<StatementNotFoundException>(problemWithoutStatement)
                }
            }
            When("problem has no pdf statement") {
                Then("throws StatementNotFoundException") {
                    downloadProblemWithException<StatementNotFoundException>(problemWithoutPdfStatement)
                }
            }
            When("problem has no cpp checker") {
                Then("throws CheckerNotFoundException") {
                    downloadProblemWithException<CheckerNotFoundException>(problemWithoutCppChecker)
                }
            }
            // Disabled because Polygon does not allow such kind of tests
            xWhen("problem has missing test indices") {
                Then("throws NonSequentialTestIndicesException") {
                    downloadProblemWithException<NonSequentialTestIndicesException>(problemWithNonSequentialTestIndices)
                }
            }
            When("sample is only second test") {
                Then("throws SamplesNotFirstException") {
                    downloadProblemWithException<SamplesNotFirstException>(problemWhereSampleGoesSecond)
                }
            }
            When("samples are first and third tests") {
                Then("throws SamplesNotFirstException") {
                    downloadProblemWithException<SamplesNotFirstException>(problemWhereSamplesAreFirstAndThirdTests)
                }
            }
            When("test groups are enabled") {
                And("some tests don't have a group") {
                    Then("throws MissingTestGroupException") {
                        downloadProblemWithException<MissingTestGroupException>(problemWithMissingTestGroups)
                    }
                }
                And("tests within the same group don't go one after another") {
                    Then("throws NonSequentialTestsInTestGroupException") {
                        downloadProblemWithException<NonSequentialTestsInTestGroupException>(
                            problemWithNonSequentialTestsInTestGroup
                        )
                    }
                }
                And("samples don't form first test group") {
                    Then("throws SamplesNotFormingFirstTestGroupException") {
                        downloadProblemWithException<SamplesNotFormingFirstTestGroupException>(
                            problemWhereSamplesAreNotFormingFirstTestGroup
                        )
                    }
                }
                And("sample has points") {
                    Then("throws PointsOnSampleException") {
                        downloadProblemWithException<PointsOnSampleException>(problemWithPointsOnSample)
                    }
                }
                And("samples group has points") {
                    Then("throws PointsOnSampleException") {
                        downloadProblemWithException<PointsOnSampleException>(problemWithPointsOnSamplesGroup)
                    }
                }
                And("points are not integral") {
                    Then("throws NonIntegralTestPointsException") {
                        downloadProblemWithException<NonIntegralTestPointsException>(problemWithNonIntegralTestPoints)
                    }
                }
                And("points are disabled") {
                    Then("throws TestPointsDisabledException") {
                        downloadProblemWithException<TestPointsDisabledException>(
                            problemWithTestGroupsButNoPointsEnabled
                        )
                    }
                }
                And("everything is alright") {
                    Then("downloads tests correctly") {
                        with(downloader.downloadProblem(problemWithNormalTestGroupsAndPoints, true)) {
                            tests shouldBe listOf(
                                IRTest(1, true, "1\r\n", "ans1\r\n", null, "samples"),
                                IRTest(2, false, "2\r\n", "ans2\r\n", null, "first"),
                                IRTest(3, false, "3\r\n", "ans3\r\n", null, "first"),
                                IRTest(4, false, "4\r\n", "ans4\r\n", 5, "second"),
                                IRTest(5, false, "5\r\n", "ans5\r\n", 5, "second"),
                                IRTest(6, false, "6\r\n", "ans6\r\n", 0, "third")
                            )
                        }
                    }
                    Then("downloads test groups correctly") {
                        with(downloader.downloadProblem(problemWithNormalTestGroupsAndPoints, false)) {
                            groups shouldBe listOf(
                                IRTestGroup("samples", IRTestGroupPointsPolicy.SAMPLES, listOf(1), null),
                                IRTestGroup("first", IRTestGroupPointsPolicy.COMPLETE_GROUP, listOf(2, 3), 10),
                                IRTestGroup("second", IRTestGroupPointsPolicy.EACH_TEST, listOf(4, 5), null),
                                IRTestGroup("third", IRTestGroupPointsPolicy.EACH_TEST, listOf(6), null),
                            )
                        }
                    }
                }
            }
        }
    }
}
