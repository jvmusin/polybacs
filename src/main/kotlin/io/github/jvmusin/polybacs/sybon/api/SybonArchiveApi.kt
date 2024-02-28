package io.github.jvmusin.polybacs.sybon.api

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange

@HttpExchange("https://archive.sybon.org/api/")
interface SybonArchiveApi {
    @GetExchange("Collections")
    suspend fun getCollections(
        @RequestParam("Offset") offset: Int = 0,
        @RequestParam("Limit") limit: Int = 100,
    ): List<SybonCollection>

    @GetExchange("Collections/{collectionId}")
    suspend fun getCollection(@PathVariable("collectionId") collectionId: Int): SybonCollection

    @GetExchange("Problems/{problemId}")
    suspend fun getProblem(@PathVariable("problemId") problemId: Int): SybonProblem

    @GetExchange("Problems/{problemId}/statement")
    suspend fun getProblemStatementUrl(@PathVariable("problemId") problemId: Int): String

    @PostExchange("Collections/{collectionId}/problems")
    suspend fun importProblem(
        @PathVariable("collectionId") collectionId: Int,
        @RequestParam("internalProblemId") internalProblemId: String,
    ): Int
}
