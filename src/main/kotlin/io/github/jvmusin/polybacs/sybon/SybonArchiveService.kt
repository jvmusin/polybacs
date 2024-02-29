package io.github.jvmusin.polybacs.sybon

import io.github.jvmusin.polybacs.sybon.api.SybonArchiveApi
import io.github.jvmusin.polybacs.sybon.api.SybonProblem
import io.github.jvmusin.polybacs.util.RetryPolicy
import org.slf4j.LoggerFactory.getLogger
import org.springframework.web.reactive.function.client.WebClientResponseException

/** Service used to communicate with Sybon Archive via [SybonArchiveApi]).
 *
 *  Uses the only collection with the given [collectionId].
 */
@Suppress("MemberVisibilityCanBePrivate")
class SybonArchiveService(
    private val sybonArchiveApi: SybonArchiveApi,
    private val collectionId: Int,
) {

    /** Returns all problems from the collection with id [collectionId]. */
    suspend fun getProblems() = sybonArchiveApi.getCollection(collectionId).problems

    /**
     * Imports problem with [bacsProblemId] to Sybon's collection [collectionId].
     *
     * (Re)tries to import the problem accordingly to [retryPolicy].
     *
     * @throws SybonProblemImportException if import failed.
     */
    suspend fun importProblem(bacsProblemId: String, retryPolicy: RetryPolicy = RetryPolicy()): SybonProblem? {
        suspend fun getProblem() = getProblems().filter { it.internalProblemId == bacsProblemId }.minByOrNull { it.id }
        getLogger(javaClass).debug("Checking if the problem is already in this collection")
        getProblem()?.let {
            getLogger(javaClass).debug("The problem is already in this collection")
            return it
        }
        getLogger(javaClass).debug("The problem is not in this collection, importing")
        retryPolicy.evalWhileNull {
            try {
                sybonArchiveApi.importProblem(collectionId, bacsProblemId)
                getLogger(javaClass).debug("Problem import into sybon succeed")
            } catch (e: WebClientResponseException) {
                if (e.statusCode.is5xxServerError) {
                    getLogger(javaClass)
                        .debug("Sybon returned 500 error on problem import, will try again")
                    null
                } else throw SybonProblemImportException("Problem import into sybon failed: ${e.message}", e)
            }
        }

        sybonArchiveApi.importProblem(collectionId, bacsProblemId)
        return retryPolicy.evalWhileNull(::getProblem)
    }
}
