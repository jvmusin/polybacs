package io.github.jvmusin.polybacs.polygon

import io.github.jvmusin.polybacs.api.StatementFormat
import io.github.jvmusin.polybacs.ir.IRProblem
import io.github.jvmusin.polybacs.polygon.api.PolygonApi
import io.github.jvmusin.polybacs.polygon.exception.downloading.ProblemDownloadingException
import io.github.jvmusin.polybacs.polygon.exception.response.NoSuchProblemException
import org.springframework.stereotype.Service

/**
 * Polygon service.
 *
 * Used to communicate to Polygon API.
 */
@Service
class PolygonService(
    private val polygonApi: PolygonApi,
    private val problemDownloader: PolygonProblemDownloader
) {

    /**
     * Downloads problem with the given [problemId] and skips tests if [includeTests] is `false` (default).
     * Additionally, you can choose a [statementFormat] between `PDF` and `HTML`.
     *
     * @throws NoSuchProblemException if the problem does not exist.
     * @throws ProblemDownloadingException if something went wrong while downloading the problem.
     */
    suspend fun downloadProblem(
        problemId: Int,
        includeTests: Boolean,
        statementFormat: StatementFormat,
        language: String,
    ): IRProblem {
        try {
            return problemDownloader.downloadProblem(problemId, includeTests, statementFormat, language)
        } catch (e: Exception) {
            throw ProblemDownloadingException("Failed to download problem $problemId: ${e.message}", e)
        }
    }

    /** Returns all known problems. */
    suspend fun getProblems() = polygonApi.getProblems().extract()

    /**
     * Returns problem information for the problem with the given [problemId].
     *
     * @throws NoSuchProblemException if the problem is not found or if access to the problem is denied.
     */
    suspend fun getProblemInfo(problemId: Int) = polygonApi.getProblemInfo(problemId).extract()

    /** Returns a set of statement languages. */
    suspend fun getProblemStatementLanguages(problemId: Int) = polygonApi.getStatements(problemId).extract().keys
}
