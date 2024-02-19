package io.github.jvmusin.polybacs.sybon

import io.github.jvmusin.polybacs.sybon.api.SybonCheckingApi
import io.github.jvmusin.polybacs.sybon.api.SybonSubmissionResult
import io.github.jvmusin.polybacs.sybon.api.SybonSubmissionResult.BuildResult
import io.github.jvmusin.polybacs.sybon.api.SybonSubmissionResult.TestGroupResult
import io.github.jvmusin.polybacs.sybon.api.SybonSubmissionResult.TestGroupResult.TestResult
import io.github.jvmusin.polybacs.sybon.api.SybonSubmitSolution
import io.github.jvmusin.polybacs.util.encodeBase64
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SybonCheckingApiTests(api: SybonCheckingApi) : StringSpec({
    "getCompilers should return all known compilers" {
        val compilers = api.getCompilers()
        compilers shouldContainAll SybonCompilers.list
    }

    "submitSolution should submit a correct solution and receive submission id" {
        val submissionId = api.submitSolution(
            SybonSubmitSolution(
                SybonCompilers.CPP.id,
                A_PLUS_B_PROBLEM_ID,
                OK_CPP_SOLUTION
            )
        )
        println(submissionId)
    }

    "getResults should return correct result for accepted C++ submission" {
        val submissionId = 466994

        val expectedSubmissionResult = SybonSubmissionResult(
            id = submissionId,
            buildResult = BuildResult(status = BuildResult.Status.OK, output = ""),
            testGroupResults = emptyList()
        )

        val expectedFirstTestGroupResult = TestGroupResult(
            internalId = "",
            executed = true,
            testResults = emptyList()
        )

        val expectedFirstTestResult = TestResult(
            status = TestResult.Status.OK,
            judgeMessage = "",
            resourceUsage = TestResult.ResourceUsage(timeUsageMillis = 1, memoryUsageBytes = 385024)
        )

        val submissionResults = api.getResults("$submissionId")
        submissionResults.shouldBeSingleton()

        val submissionResult = submissionResults.single()
        submissionResult.copy(testGroupResults = emptyList()) shouldBe expectedSubmissionResult
        submissionResult.testGroupResults shouldHaveSize 1

        val testResults = submissionResult.testGroupResults.single()
        testResults.copy(testResults = emptyList()) shouldBe expectedFirstTestGroupResult
        testResults.testResults shouldHaveSize 10
        testResults.testResults.first() shouldBe expectedFirstTestResult
    }

    "getResults with two ids should return sorted by id results" {
        val ids = listOf(467018, 467020, 467019)
        val s = ids.joinToString(",")
        api.getResults(s).map { it.id } shouldBe ids.sorted()
    }

    "Test getResults test groups results for accepted C++ solution" {
        val submissionId = 466994
        val result = api.getResults(submissionId.toString()).single()
        println(Json { prettyPrint = true }.encodeToString(result))
    }
}) {
    companion object {
        const val A_PLUS_B_PROBLEM_ID = 8716

        @Suppress("SpellCheckingInspection")
        val OK_CPP_SOLUTION = """
        #include <iostream>
        int main() {
          long long x, y;
          std::cin >> x >> y;
          std::cout << x + y;
          return 0;
        }
        """.trimIndent().encodeBase64()
    }
}
