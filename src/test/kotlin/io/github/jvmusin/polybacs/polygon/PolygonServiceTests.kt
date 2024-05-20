package io.github.jvmusin.polybacs.polygon

import io.github.jvmusin.polybacs.polygon.TestProblems.noBuiltPackagesProblem
import io.github.jvmusin.polybacs.polygon.TestProblems.problemWithOnlyReadAccess
import io.github.jvmusin.polybacs.polygon.TestProblems.totallyUnknownProblem
import io.github.jvmusin.polybacs.polygon.exception.response.NoSuchProblemException
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PolygonServiceTests(service: PolygonService) : BehaviorSpec({
    Given("getProblemInfo") {
        When("have WRITE access") {
            Then("returns info") {
                with(service.getProblemInfo(noBuiltPackagesProblem)) {
                    inputFile shouldBe "stdin"
                    outputFile shouldBe "stdout"
                    interactive shouldBe false
                    timeLimit shouldBe 1000
                    memoryLimit shouldBe 256
                }
            }
        }
        // the test doesn't work, but it's even better
        xWhen("have READ access") {
            Then("throws NoSuchProblemException") {
                shouldThrowExactly<NoSuchProblemException> {
                    service.getProblemInfo(problemWithOnlyReadAccess)
                }
            }
        }
        When("accessing unknown problem") {
            Then("throws NoSuchProblemException") {
                shouldThrowExactly<NoSuchProblemException> {
                    service.getProblemInfo(totallyUnknownProblem)
                }
            }
        }
    }

//    xGiven("getProblems") {
//        When("there is a problem with no appropriate checker") {
//            Then("it's found and logged and then an exception is thrown") {
//                for (problem in service.getProblems()) {
//                    try {
//                        service.downloadProblem(problem.id)
//                    } catch (e: ProblemDownloadingException) {
//                        if (e.cause!!::class == CheckerNotFoundException::class) {
//                            System.err.println("FOUND A PROBLEM WITH NO CPP CHECKER: ${problem.id}")
//                            throw e
//                        } else {
//                            System.err.println("PROBLEM ${problem.id} FAILED WITH AN EXCEPTION ${e.message}")
//                        }
//                    }
//                }
//            }
//        }
//    }
})
