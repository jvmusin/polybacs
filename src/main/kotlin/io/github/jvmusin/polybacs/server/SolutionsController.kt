package io.github.jvmusin.polybacs.server

import io.github.jvmusin.polybacs.OffloadScope
import io.github.jvmusin.polybacs.WebSocketConnectionKeeper
import io.github.jvmusin.polybacs.api.*
import io.github.jvmusin.polybacs.bacs.BacsArchiveService
import io.github.jvmusin.polybacs.polygon.PolygonService
import io.github.jvmusin.polybacs.sybon.SybonArchiveService
import io.github.jvmusin.polybacs.sybon.SybonCheckingService
import io.github.jvmusin.polybacs.sybon.SybonSolutionTestingTimeoutException
import io.github.jvmusin.polybacs.sybon.converter.IRLanguageToCompilerConverter.toSybonCompiler
import io.github.jvmusin.polybacs.sybon.converter.SybonSubmissionResultToSubmissionResultConverter.toSubmissionResult
import jakarta.servlet.http.HttpSession
import kotlinx.coroutines.CoroutineExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/problems/{problemId}/solutions")
class SolutionsController(
    private val polygonService: PolygonService,
    private val webSocketConnectionKeeper: WebSocketConnectionKeeper,
    private val bacsArchiveService: BacsArchiveService,
    private val testSybonArchiveService: SybonArchiveService,
    private val sybonCheckingService: SybonCheckingService,
    private val offloadScope: OffloadScope,
    private val statusTracker: StatusTracker,
) {
    @GetMapping
    suspend fun getSolutions(@PathVariable problemId: Int): List<Solution> {
        val problem = polygonService.downloadProblem(problemId)
        return problem.solutions.map {
            Solution(
                it.name,
                Language.valueOf(it.language.name),
                Verdict.valueOf(it.verdict.name)
            )
        }
    }

    private fun exceptionHandler(action: String, session: HttpSession) =
        CoroutineExceptionHandler { _, throwable ->
            val toastSender = webSocketConnectionKeeper.createSender(session.id)
            toastSender.send("Failed to $action: ${throwable.message}", ToastKind.FAILURE)
        }

    @RequestMapping("/createTestProblem")
    fun createTestProblem(@PathVariable problemId: Int, session: HttpSession) =
        offloadScope.launch(exceptionHandler("create test problem", session)) {
            val problemName = polygonService.downloadProblem(problemId).name
            val properties = AdditionalProblemProperties(name = problemName, suffix = "-test")
            val fullName = properties.buildFullName()

            val updateConsumer = statusTracker.newTrack(problemId, problemName, session.id)
            transferProblemToBacs(updateConsumer, problemId, properties, false, polygonService, bacsArchiveService)

            updateConsumer.consumeUpdate("Ждём, пока задача появится в сайбоне (может занять минуты две)", StatusTrackUpdateSeverity.NEUTRAL)
            val sybonProblem = testSybonArchiveService.importProblem(fullName)
            if (sybonProblem == null) {
                updateConsumer.consumeUpdate("Задача так и не появилась в сайбоне", StatusTrackUpdateSeverity.FAILURE)
                return@launch
            }
            updateConsumer.consumeUpdate("Задача появилась в сайбоне: ${sybonProblem.id}", StatusTrackUpdateSeverity.SUCCESS)
        }

    @RequestMapping("/test")
    fun testSolution(@PathVariable problemId: Int, sybonProblemId: Int, solutionName: String, session: HttpSession) =
        offloadScope.launch(exceptionHandler("test solution", session)) {
            val problem = polygonService.downloadProblem(problemId, true)

            val solution = problem.solutions.single { it.name == solutionName }
            val compiler = solution.language.toSybonCompiler()
            val result =
                if (compiler == null) {
                    SubmissionResult(Verdict.NOT_TESTED, message = "Сайбон не знает про ${solution.language.fullName}")
                } else try {
                    sybonCheckingService.submitSolutionTimed(sybonProblemId, solution.content, compiler)
                        .toSubmissionResult()
                } catch (e: SybonSolutionTestingTimeoutException) {
                    SubmissionResult(Verdict.SERVER_ERROR, message = e.message)
                }

            val toastSender = webSocketConnectionKeeper.createSender(session.id)
            toastSender.send("Test result for $solutionName: $result")
        }
}
