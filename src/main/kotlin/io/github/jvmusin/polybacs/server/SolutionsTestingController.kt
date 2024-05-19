//package io.github.jvmusin.polybacs.server
//
//import io.github.jvmusin.polybacs.OffloadScope
//import io.github.jvmusin.polybacs.WebSocketConnectionKeeper
//import io.github.jvmusin.polybacs.api.AdditionalProblemProperties
//import io.github.jvmusin.polybacs.api.SubmissionResult
//import io.github.jvmusin.polybacs.api.ToastKind
//import io.github.jvmusin.polybacs.api.Verdict
//import io.github.jvmusin.polybacs.bacs.BacsArchiveService
//import io.github.jvmusin.polybacs.polygon.PolygonService
//import io.github.jvmusin.polybacs.sybon.SybonArchiveService
//import io.github.jvmusin.polybacs.sybon.SybonCheckingService
//import io.github.jvmusin.polybacs.sybon.SybonSolutionTestingTimeoutException
//import io.github.jvmusin.polybacs.sybon.converter.IRLanguageToCompilerConverter.toSybonCompiler
//import io.github.jvmusin.polybacs.sybon.converter.SybonSubmissionResultToSubmissionResultConverter.toSubmissionResult
//import jakarta.servlet.http.HttpSession
//import kotlinx.coroutines.CoroutineExceptionHandler
//import org.springframework.web.bind.annotation.PathVariable
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RestController
//
//@RestController
//@RequestMapping("/api/problems/{problemId}/solutions/testing")
//class SolutionsTestingController(
//    private val polygonService: PolygonService,
//    private val webSocketConnectionKeeper: WebSocketConnectionKeeper,
//    private val bacsArchiveService: BacsArchiveService,
//    private val testSybonArchiveService: SybonArchiveService,
//    private val sybonCheckingService: SybonCheckingService,
//    private val offloadScope: OffloadScope,
//    private val statusTracker: StatusTracker,
//) {
//    private fun exceptionHandler(action: String, session: HttpSession) =
//        CoroutineExceptionHandler { _, throwable ->
//            val toastSender = webSocketConnectionKeeper.createSender(session.id)
//            toastSender.send("Failed to $action: ${throwable.message}", ToastKind.FAILURE)
//        }
//
//    // TODO: This method is not in use yet, so can be refactored and rewritten freely
//    @RequestMapping("/createTestProblem")
//    fun createTestProblem(@PathVariable problemId: Int, session: HttpSession) =
//        offloadScope.launch(exceptionHandler("create test problem", session)) {
//            val language = "russian" // TODO: Drop?
//            val problem = polygonService.downloadProblem(problemId, language = language) // TODO: Add other props
//            val problemName = problem.name
//            val properties = AdditionalProblemProperties(name = problemName, suffix = "-test", language = language)
//            val fullName = properties.buildFullName()
//
//            val updateConsumer = statusTracker.newTrack(problemId, problemName, session.id)
//            transferProblemToBacs(updateConsumer, problemId, properties, false, polygonService, bacsArchiveService)
//
//            updateConsumer.consumeUpdate(
//                "Ждём, пока задача появится в сайбоне (может занять минуты две)",
//                StatusTrackUpdateSeverity.NEUTRAL
//            )
//            val sybonProblem = testSybonArchiveService.importProblem(fullName)
//            if (sybonProblem == null) {
//                updateConsumer.consumeUpdate("Задача так и не появилась в сайбоне", StatusTrackUpdateSeverity.FAILURE)
//                return@launch
//            }
//            updateConsumer.consumeUpdate(
//                "Задача появилась в сайбоне: ${sybonProblem.id}",
//                StatusTrackUpdateSeverity.SUCCESS
//            )
//        }
//
//    @RequestMapping("/test")
//    fun testSolution(@PathVariable problemId: Int, sybonProblemId: Int, solutionName: String, session: HttpSession) =
//        offloadScope.launch(exceptionHandler("test solution", session)) {
//            val problem = polygonService.downloadProblem(problemId, true)
//
//            val solution = problem.solutions.single { it.name == solutionName }
//            val compiler = solution.language.toSybonCompiler()
//            val result =
//                if (compiler == null) {
//                    SubmissionResult(Verdict.NOT_TESTED, message = "Сайбон не знает про ${solution.language.fullName}")
//                } else try {
//                    sybonCheckingService.submitSolutionTimed(sybonProblemId, solution.content, compiler)
//                        .toSubmissionResult()
//                } catch (e: SybonSolutionTestingTimeoutException) {
//                    SubmissionResult(Verdict.SERVER_ERROR, message = e.message)
//                }
//
//            val toastSender = webSocketConnectionKeeper.createSender(session.id)
//            toastSender.send("Test result for $solutionName: $result")
//        }
//}
