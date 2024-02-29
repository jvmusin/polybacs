package io.github.jvmusin.polybacs.sybon.api

import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange

@HttpExchange("https://checking.sybon.org/api/")
interface SybonCheckingApi {
    @GetExchange("Compilers")
    suspend fun getCompilers(): List<SybonCompiler>

    @PostExchange("Submits/send")
    suspend fun submitSolution(@RequestBody solution: SybonSubmitSolution): Int

    @PostExchange("Submits/sendall")
    suspend fun submitSolutions(@RequestBody solutions: List<SybonSubmitSolution>): List<Int>

    @PostExchange("Submits/rejudge")
    suspend fun rejudge(@RequestBody ids: List<Int>)

    @GetExchange("Submits/results")
    suspend fun getResults(@RequestParam("ids") ids: String): List<SybonSubmissionResult>
}
