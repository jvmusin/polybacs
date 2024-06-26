package io.github.jvmusin.polybacs.polygon

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.jvmusin.polybacs.api.StatementFormat
import io.github.jvmusin.polybacs.ir.*
import io.github.jvmusin.polybacs.polygon.api.*
import io.github.jvmusin.polybacs.polygon.converter.PolygonPointsPolicyConverter
import io.github.jvmusin.polybacs.polygon.converter.PolygonSourceTypeToIRLanguageConverter
import io.github.jvmusin.polybacs.polygon.converter.PolygonTagToIRVerdictConverter
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
import io.github.jvmusin.polybacs.polygon.exception.response.TestGroupsDisabledException
import io.github.jvmusin.polybacs.util.sequentiallyGroupedBy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import org.springframework.util.ConcurrentReferenceHashMap

/**
 * Polygon problem downloader
 *
 * Used for downloading problems from Polygon.
 */
@Component
class PolygonProblemDownloader(
    private val polygonApi: PolygonApi,
) {

    /**
     * Full package id.
     *
     * Used as a key for the cache of problems.
     *
     * @property packageId Id of problem's package.
     * @property includeTests Includes tests or not.
     */
    private data class FullPackageId(
        val packageId: Int,
        val includeTests: Boolean,
        val statementFormat: StatementFormat,
        val language: String,
    )

    /**
     * Problems cache.
     */
    private val cache = ConcurrentReferenceHashMap<FullPackageId, IRProblem>()

    /**
     * Returns problem using Polygon API.
     *
     * @param problemId id of the problem.
     * @return Problem with given [problemId] from Polygon API.
     * @throws ProblemModifiedException if the problem has uncommitted changes.
     * @throws NoPackagesBuiltException if the problem has no built packages.
     * @throws OldBuiltPackageException if latest built package for the problem is not for the latest revision.
     */
    private suspend fun getProblem(problemId: Int): Problem {
        return polygonApi.getProblem(problemId).apply {
            if (modified) {
                throw ProblemModifiedException(
                    "Problem is modified. Rollback or commit changes."
                )
            }
            if (latestPackage == null) {
                throw NoPackagesBuiltException("There are no built packages for the problem. Build a new package.")
            }
            if (latestPackage != revision) {
                throw OldBuiltPackageException("There is no built package for the latest revision. Build a new package.")
            }
        }
    }

    /**
     * Returns problem info from Polygon API.
     *
     * @param problemId id of the problem.
     * @return Problem info for the problem with given [problemId] from Polygon API.
     * @throws UnsupportedFormatException if the problem has unsupported format.
     */
    private suspend fun getProblemInfo(problemId: Int): ProblemInfo {
        return polygonApi.getProblemInfo(problemId).extract().apply {
            if (interactive) {
                throw UnsupportedFormatException("Interactive problems are not supported.")
            }
        }
    }

    private fun ByteArray.fixExternalLinks(format: StatementFormat): ByteArray {
        return if (format == StatementFormat.HTML) {
            val mathJaxWas = """
                <SCRIPT async="" src="https://polygon.codeforces.com/lib/MathJax/MathJax.js?config=TeX-MML-AM_CHTML" type="text/javascript">
            """.trimIndent()
            val mathJaxNew = """
                <SCRIPT async="" src="https://statement.bacs.cs.istu.ru/MathJax.js?config=TeX-MML-AM_CHTML" type="text/javascript">
            """.trimIndent()

            fun String.replaceSurely(was: String, new: String) = replace(was, new).also {
                require(it != this) {
                    "String $was had to be in the file and replaced with $new, but it was not found in $this"
                }
            }
            decodeToString()
                .replaceSurely(mathJaxWas, mathJaxNew)
                .encodeToByteArray()
        } else {
            this
        }
    }

    /**
     * Returns problem statement.
     *
     * @param problemId id of the problem.
     * @param packageId id of the problem package.
     * @param format format of the statement be it `PDF` or `HTML`.
     * @return An [IRStatement] for the given problem.
     * @throws StatementNotFoundException when the statement for the requested format/language not found.
     */
    private suspend fun downloadStatement(
        problemId: Int,
        packageId: Int,
        format: StatementFormat,
        language: String,
    ): IRStatement {
        val statement = polygonApi.getStatement(problemId, language)
            ?: throw StatementNotFoundException("Statement on $language in $format not found")

        val files = polygonApi.getStatementFiles(problemId, packageId, format, language)
        val mainProblemFileName = "problem.${format.lowercase}"
        if (files.keys.none { it == mainProblemFileName })
            throw StatementNotFoundException("$format statement not found")
        val extraFiles = files.map {
            IRStatementExtraFile(
                it.key,
                if (it.key == mainProblemFileName) it.value.fixExternalLinks(format) else it.value
            )
        }
        return IRStatement(statement.name, extraFiles, format)
    }

    /**
     * Returns problem checker.
     *
     * @param problemId id of the problem.
     * @param packageId if of the problem package.
     * @return Problem checker.
     * @throws CheckerNotFoundException if checker is not found or is not in *.cpp* format.
     */
    private suspend fun downloadChecker(problemId: Int, packageId: Int): IRChecker {
        val name = "check.cpp"
        val file = polygonApi.getFileFromZipPackage(problemId, packageId, name)
            ?: throw CheckerNotFoundException(
                "Checker named '$name' not found. Other checkers are bot supported"
            )
        return IRChecker(name, file.decodeToString())
    }

    /**
     * Returns problem solutions.
     *
     * @param problemId id of the problem.
     * @return Problem solutions.
     */
    private suspend fun getSolutions(problemId: Int): List<IRSolution> {
        val solutions =
            polygonApi.getSolutionsFromZipPackage(problemId, polygonApi.getLatestPackageId(problemId))
        return polygonApi.getSolutions(problemId).extract().map { solution ->
            val solutionWithDescription = requireNotNull(solutions[solution.name]) {
                "Solution ${solution.name} is returned via Polygon API but not found in the problem archive"
            }
            IRSolution(
                name = solution.name,
                verdict = PolygonTagToIRVerdictConverter.convert(solution.tag),
                isMain = solution.tag == "MA",
                language = PolygonSourceTypeToIRLanguageConverter.convert(solution.sourceType),
                content = solutionWithDescription.solution,
                description = solutionWithDescription.description,
            )
        }
    }

    /**
     * Validates tests and test groups.
     *
     * Test indices should go from **1** to **number of tests**.
     * If it's not so, then [NonSequentialTestIndicesException] is thrown.
     *
     * Samples should go before ordinal tests.
     * If it's not so, [SamplesNotFirstException] is thrown.
     *
     * When test groups are enabled, then all tests should have test group.
     * If it's not so, then [MissingTestGroupException] is thrown.
     *
     * All tests within the same test group should go one-after-another.
     * There should be no two tests from the same test group
     * when there is a test with some other test group between them.
     * If it's not so, then [NonSequentialTestsInTestGroupException] is thrown.
     *
     * Samples should form the first test group.
     * If it's not so, then [SamplesNotFormingFirstTestGroupException] is thrown.
     *
     * @param tests tests for the problem.
     * @throws NonSequentialTestIndicesException if test indices don't go from 1 to its count.
     * @throws SamplesNotFirstException if samples don't go first.
     * @throws MissingTestGroupException if some tests have test groups and some don't (if test groups enabled).
     * @throws NonSequentialTestsInTestGroupException if test groups don't go sequentially (if test groups enabled).
     * @throws SamplesNotFormingFirstTestGroupException if samples don't form the first test group (if test groups enabled).
     */
    private fun validateTests(tests: List<PolygonTest>, testGroupsEnabled: Boolean) {
        if (tests.withIndex().any { (index, test) -> index + 1 != test.index }) {
            throw NonSequentialTestIndicesException("Tests numbers should go from 1 to their count")
        }

        val samples = tests.filter { it.useInStatements }
        val anySamples = samples.any()

        if (anySamples) {
            if (!tests.first().useInStatements || tests.sequentiallyGroupedBy { it.useInStatements }.size > 2) {
                throw SamplesNotFirstException("Samples should go before any other tests")
            }
        }

        if (!testGroupsEnabled) return

        val groups = tests.sequentiallyGroupedBy { it.group }
        if (groups.size != groups.distinctBy { it.key }.size) {
            throw NonSequentialTestsInTestGroupException("Tests from a single group should go together")
        }
        if (anySamples) {
            if (groups.first().size != samples.size) {
                throw SamplesNotFormingFirstTestGroupException("Samples should be the first test group")
            }
            if (samples.any { it.points != 0.0 }) {
                throw PointsOnSampleException("Samples should give points")
            }
        }
    }

    /**
     * Returns test groups.
     *
     * If test groups are disabled for the problem, returns *null*.
     *
     * Points for the test group are set iff points policy is set to **COMPLETE_GROUP**.
     * In such case, points for the test group are equal to the sum of points of tests within this test group.
     * Otherwise, points are set to *null*.
     *
     * @param problemId id of the problem.
     * @param rawTests raw Polygon tests.
     * @return Test groups or *null* if test groups are disabled.
     * @throws MissingTestGroupException if test groups are enabled and there are tests without test group set.
     * @throws TestPointsDisabledException if test groups are enabled, but test points are not.
     * @throws NonIntegralTestPointsException if tests have non-integral points.
     */
    private suspend fun getTestGroups(problemId: Int, rawTests: List<PolygonTest>): List<IRTestGroup>? {
        val rawTestGroups = try {
            polygonApi.getTestGroup(problemId).extract()
        } catch (e: TestGroupsDisabledException) {
            return null
        }

        if (rawTests.any { it.group == null }) {
            throw MissingTestGroupException("Test group should be set for all tests")
        }
        if (rawTests.any { it.points == null }) {
            throw TestPointsDisabledException(
                "To use test groups, toggle on the 'Are test points enabled?' option in Polygon"
            )
        }
        if (rawTests.any { it.points != it.points!!.toInt().toDouble() }) {
            throw NonIntegralTestPointsException("Test points should be integers")
        }

        val groups = rawTestGroups.associateBy { it.name }
        return rawTests.sequentiallyGroupedBy { it.group!! }.map { (groupName, tests) ->
            val pointsPolicy = when {
                tests.any { it.useInStatements } -> IRTestGroupPointsPolicy.SAMPLES
                else -> PolygonPointsPolicyConverter.convert(groups[groupName]!!.pointsPolicy)
            }
            val points = when (pointsPolicy) {
                IRTestGroupPointsPolicy.COMPLETE_GROUP -> tests.sumOf { it.points!!.toInt() }
                else -> null
            }
            IRTestGroup(groupName, pointsPolicy, tests.map { it.index }, points)
        }
    }

    /**
     * Returns problem tests and test groups.
     *
     * Tests are validated using [validateTests].
     *
     * If test groups are disabled, then returns *null* test groups.
     *
     * If tests are skipped via *[includeTests] = false*, then returns *null* tests.
     *
     * Points for the test are set iff test groups are enabled
     * and the corresponding test group has **EACH_TEST** points policy.
     *
     * @param problemId id of the problem.
     * @param includeTests whether to include tests or not.
     * @return Pair of problem tests and test groups.
     */
    private suspend fun getTestsAndTestGroups(problemId: Int, includeTests: Boolean) = coroutineScope {
        val rawTests = polygonApi.getTests(problemId).extract().sortedBy { it.index }
        val testGroups = getTestGroups(problemId, rawTests)
        validateTests(rawTests, testGroups != null)

        if (!includeTests) return@coroutineScope null to testGroups

        val testGroupsByName = testGroups?.associateBy { it.name }
        val inputs = rawTests.map { async { polygonApi.getTestInput(problemId, it.index) } }
        val answers = rawTests.map { async { polygonApi.getTestAnswer(problemId, it.index) } }
        val tests = rawTests.indices.map { i ->
            val test = rawTests[i]
            val group = testGroupsByName?.get(test.group)
            val points = group
                ?.takeIf { it.pointsPolicy == IRTestGroupPointsPolicy.EACH_TEST }
                ?.let { test.points!!.toInt() }

            @Suppress("USELESS_ELVIS") // empty response somehow becomes null
            val input = inputs[i].await() ?: byteArrayOf()

            @Suppress("USELESS_ELVIS") // empty response somehow becomes null
            val output = answers[i].await() ?: byteArrayOf()
            IRTest(test.index, test.useInStatements, input, output, points, test.group)
        }
        tests to testGroups
    }

    /**
     * Returns problem from the cache or *null* if it's not in the cache.
     *
     * @param packageId id of the problem.
     * @param includeTests whether to include tests or not.
     * @param statementFormat format of the statement, be it `PDF` or `HTML`.
     * @return [IRProblem] instance of the problem or *null* if it's not in the cache.
     */
    private fun getProblemFromCache(
        packageId: Int,
        includeTests: Boolean,
        statementFormat: StatementFormat,
        language: String
    ): IRProblem? {
        cache[FullPackageId(packageId, includeTests, statementFormat, language)].also { if (it != null) return it }
        if (!includeTests) {
            cache[FullPackageId(packageId, true, statementFormat, language)].also { if (it != null) return it }
        }
        return null
    }

    /**
     * Saves problem into the cache.
     *
     * @param packageId if of the problem.
     * @param includeTests whether to include tests or not.
     * @param statementFormat format of the statement, be it `PDF` or `HTML`.
     * @param problem [IRProblem] instance to save.
     */
    private fun saveProblemToCache(
        packageId: Int,
        includeTests: Boolean,
        statementFormat: StatementFormat,
        language: String,
        problem: IRProblem,
    ) {
        cache[FullPackageId(packageId, includeTests, statementFormat, language)] = problem
    }

    /**
     * Downloads the problem with the given [problemId].
     *
     * Tests might be skipped by setting [includeTests].
     *
     * @param problemId id of the problem to download.
     * @param includeTests if true then the problem tests will also be downloaded.
     * @return The problem with or without tests, depending on [includeTests] parameter.
     * @throws NoSuchProblemException if the problem does not exist.
     * @throws ProblemDownloadingException if something gone wrong while downloading the problem.
     */
    suspend fun downloadProblem(
        problemId: Int,
        includeTests: Boolean,
        statementFormat: StatementFormat = StatementFormat.PDF,
        language: String
    ): IRProblem = withContext(Dispatchers.IO) {
        // eagerly check for access
        val problem = getProblem(problemId)

        val packageId = polygonApi.getLatestPackageId(problemId)

        val cached = getProblemFromCache(packageId, includeTests, statementFormat, language)
        if (cached != null) return@withContext cached

        val info = async { getProblemInfo(problemId) }
        val statement = async { downloadStatement(problemId, packageId, statementFormat, language) }
        val checker = async { downloadChecker(problemId, packageId) }

        val testsAndTestGroups = async {
            /*
             * These methods can throw an exception about incorrectly formatted problem,
             * so throw them as soon as possible before downloading tests data.
             */
            run {
                info.await()
                statement.await()
                checker.await()
            }
            getTestsAndTestGroups(problemId, includeTests)
        }

        val solutions = async { getSolutions(problemId) }
        val limits = async { with(info.await()) { IRLimits(timeLimit, memoryLimit) } }
        val problemXml = async {
            requireNotNull(polygonApi.getFileFromZipPackage(problemId, packageId, "problem.xml")) {
                "problem.xml not found in the archive"
            }
        }
        val filesFiles = async {
            polygonApi.getFilesFromZipPackage(problemId, packageId, "files") {
                it != "olymp.sty" &&
                        it != "testlib.h" &&
                        it != "statements.ftl" &&
                        !it.endsWith(".jar") &&
                        !it.endsWith(".exe")
            }
        }
        val statementsFiles =
            async { polygonApi.getFilesFromZipPackage(problemId, packageId, "statements") { !it.startsWith('.') } }

        val miscFiles = async {
            listOf(
                IRMiscFile("materials/problem.xml", problemXml.await()),
            ) +
                    filesFiles.await().map { IRMiscFile("materials/files/${it.key}", it.value) } +
                    statementsFiles.await().map { IRMiscFile("materials/statements/${it.key}", it.value) }
        }

        val polygonUrl = async {
            val xml = problemXml.await().decodeToString()
            val mapper = XmlMapper().registerKotlinModule()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

            data class ProblemXml(val url: String)

            val parsedProblemXml = mapper.readValue<ProblemXml>(xml)
            parsedProblemXml.url
        }

        IRProblem(
            name = problem.name,
            owner = problem.owner,
            statement = statement.await(),
            limits = limits.await(),
            tests = testsAndTestGroups.await().first,
            groups = testsAndTestGroups.await().second,
            checker = checker.await(),
            solutions = solutions.await(),
            miscFiles = miscFiles.await(),
            revision = problem.revision,
            polygonUrl = polygonUrl.await()
        ).also { saveProblemToCache(packageId, includeTests, statementFormat, language, it) }
    }
}
