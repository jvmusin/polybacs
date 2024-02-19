package io.github.jvmusin.polybacs.polygon

import io.github.jvmusin.polybacs.polygon.api.PolygonApi
import io.github.jvmusin.polybacs.polygon.exception.response.NoSuchProblemException
import io.github.jvmusin.polybacs.polygon.exception.response.NoSuchTestGroupException
import io.github.jvmusin.polybacs.polygon.exception.response.TestGroupsDisabledException
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PolygonResponseTests(api: PolygonApi) : BehaviorSpec({
    Given("extract") {
        When("requested unknown problem") {
            Then("throws NoSuchProblemException") {
                shouldThrowExactly<NoSuchProblemException> {
                    api.getProblemInfo(TestProblems.totallyUnknownProblem).extract()
                }
            }
        }
        When("requested test groups from problem with no test groups") {
            Then("throws TestGroupsDisabledException") {
                shouldThrowExactly<TestGroupsDisabledException> {
                    api.getTestGroup(TestProblems.problemWithoutPdfStatement).extract()
                }
            }
        }
        When("requested unknown test group") {
            Then("throws NoSuchTestGroupException") {
                shouldThrowExactly<NoSuchTestGroupException> {
                    api.getTestGroup(TestProblems.problemWithTestGroups, "unknown-test-group").extract()
                }
            }
        }
        When("requested normal problem") {
            Then("returns correct result") {
                with(api.getProblemInfo(TestProblems.problemWithTestGroups).extract()) {
                    inputFile shouldBe "stdin"
                    outputFile shouldBe "stdout"
                    interactive shouldBe false
                    timeLimit shouldBe 1000
                    memoryLimit shouldBe 256
                }
            }
        }
    }
})
