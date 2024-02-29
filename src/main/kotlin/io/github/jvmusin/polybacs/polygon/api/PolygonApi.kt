package io.github.jvmusin.polybacs.polygon.api

import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange

/**
 * Polygon API.
 *
 * Provides access to Polygon API.
 */
@HttpExchange("https://polygon.codeforces.com/api/")
@Suppress("unused")
interface PolygonApi {
    companion object {
        const val DEFAULT_TESTSET = "tests"
    }

    @PostExchange("problems.list")
    suspend fun getProblems(
        @RequestParam("showDeleted") showDeleted: Boolean = false,
        @RequestParam("id", required = false) id: Int? = null,
        @RequestParam("name", required = false) name: String? = null,
        @RequestParam("owner", required = false) owner: String? = null,
    ): PolygonResponse<List<Problem>>

    @PostExchange("problem.info")
    suspend fun getProblemInfo(
        @RequestParam("problemId") problemId: Int,
    ): PolygonResponse<ProblemInfo>

    @PostExchange("problem.statements")
    suspend fun getStatements(
        @RequestParam("problemId") problemId: Int,
    ): PolygonResponse<Map<String, Statement>>

    @PostExchange("problem.statementResources")
    suspend fun getStatementResources(
        @RequestParam("problemId") problemId: Int,
    ): PolygonResponse<List<File>>

    @PostExchange("problem.checker")
    suspend fun getCheckerName(
        @RequestParam("problemId") problemId: Int,
    ): PolygonResponse<String>

    @PostExchange("problem.validator")
    suspend fun getValidatorName(
        @RequestParam("problemId") problemId: Int,
    ): PolygonResponse<String>

    @PostExchange("problem.interactor")
    suspend fun getInteractorName(
        @RequestParam("problemId") problemId: Int,
    ): PolygonResponse<String>

    @PostExchange("problem.files")
    suspend fun getFiles(
        @RequestParam("problemId") problemId: Int,
    ): PolygonResponse<Map<String, List<File>>>

    @PostExchange("problem.solutions")
    suspend fun getSolutions(
        @RequestParam("problemId") problemId: Int,
    ): PolygonResponse<List<Solution>>

    @PostExchange("problem.viewFile")
    suspend fun getFile(
        @RequestParam("problemId") problemId: Int,
        @RequestParam("type") type: File.Type,
        @RequestParam("name") name: String,
    ): ByteArray // TODO: Check with documentation

    @PostExchange("problem.viewSolution")
    suspend fun getSolutionContent(
        @RequestParam("problemId") problemId: Int,
        @RequestParam("name") name: String,
    ): String

    @PostExchange("problem.script")
    suspend fun getScript(
        @RequestParam("problemId") problemId: Int,
        @RequestParam("testset") testset: String,
    ): ByteArray // TODO: Check with documentation

    @PostExchange("problem.tests")
    suspend fun getTests(
        @RequestParam("problemId") problemId: Int,
        @RequestParam("testset") testSet: String = DEFAULT_TESTSET,
    ): PolygonResponse<List<PolygonTest>>

    @PostExchange("problem.testInput")
    suspend fun getTestInput(
        @RequestParam("problemId") problemId: Int,
        @RequestParam("testIndex") testIndex: Int,
        @RequestParam("testset") testSet: String = DEFAULT_TESTSET,
    ): String

    @PostExchange("problem.testAnswer")
    suspend fun getTestAnswer(
        @RequestParam("problemId") problemId: Int,
        @RequestParam("testIndex") testIndex: Int,
        @RequestParam("testset") testSet: String = DEFAULT_TESTSET,
    ): String

    @PostExchange("problem.viewTestGroup")
    suspend fun getTestGroup(
        @RequestParam("problemId") problemId: Int,
        @RequestParam("group", required = false) group: String? = null,
        @RequestParam("testset") testset: String = DEFAULT_TESTSET, // TODO: Check if default value works
    ): PolygonResponse<List<TestGroup>>

    @PostExchange("problem.viewTags")
    suspend fun getTags(
        @RequestParam("problemId") problemId: Int,
    ): PolygonResponse<List<String>>

    @PostExchange("problem.viewGeneralDescription")
    suspend fun getGeneralDescription(
        @RequestParam("problemId") problemId: Int,
    ): PolygonResponse<String>

    @PostExchange("problem.viewGeneralTutorial")
    suspend fun getGeneralTutorial(
        @RequestParam("problemId") problemId: Int,
    ): PolygonResponse<String>

    @PostExchange("problem.packages")
    suspend fun getPackages(
        @RequestParam("problemId") problemId: Int,
    ): PolygonResponse<List<Package>>

    @PostExchange("problem.package")
    suspend fun getPackage(
        @RequestParam("problemId") problemId: Int,
        @RequestParam("packageId") packageId: Int,
    ): ByteArray // TODO: Check if it works

    @PostExchange("contest.problems")
    suspend fun getContestProblems(
        @RequestParam("contestId") contestId: Int,
    ): PolygonResponse<List<Problem>>
}
