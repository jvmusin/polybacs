package io.github.jvmusin.polybacs.server

import io.github.jvmusin.polybacs.ToastSender
import io.github.jvmusin.polybacs.WebSocketConnectionKeeper
import io.github.jvmusin.polybacs.api.AdditionalProblemProperties
import io.github.jvmusin.polybacs.api.ProblemInfo
import io.github.jvmusin.polybacs.api.StatementFormat
import io.github.jvmusin.polybacs.api.ToastKind
import io.github.jvmusin.polybacs.bacs.BacsArchiveService
import io.github.jvmusin.polybacs.ir.IRProblem
import io.github.jvmusin.polybacs.polygon.PolygonService
import io.github.jvmusin.polybacs.polygon.api.toDto
import io.github.jvmusin.polybacs.polygon.exception.downloading.ProblemDownloadingException
import io.github.jvmusin.polybacs.sybon.toZipArchive
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import kotlin.io.path.name

@RestController
@RequestMapping("/problems/{problemId}")
class ProblemController(
    private val bacsArchiveService: BacsArchiveService,
    private val polygonService: PolygonService,
    private val webSocketConnectionKeeper: WebSocketConnectionKeeper,
) {
    @GetMapping
    suspend fun getProblem(@PathVariable problemId: Int): ProblemInfo {
        val problemInfo = polygonService.getProblemInfo(problemId)
        return problemInfo.toDto()
    }

    @PostMapping
    @RequestMapping("/download")
    suspend fun download(
        @PathVariable problemId: Int,
        fullName: String,
        @RequestBody properties: AdditionalProblemProperties,
        session: HttpSession,
        response: HttpServletResponse,
    ) {
        val toastSender = webSocketConnectionKeeper.createSender(session.id)
        val irProblem = downloadProblem(toastSender, problemId, polygonService, properties.statementFormat)
        val zip = irProblem.toZipArchive(properties)
        toastSender.send("Задача выкачана из полигона, скачиваем архив", ToastKind.SUCCESS)
        response.addHeader(
            "Content-Disposition",
            "attachment; filename=${zip.name}"
        )
        response.outputStream.use { zip.toFile().inputStream().copyTo(it) }
    }

    @PostMapping
    @RequestMapping("/transfer")
    suspend fun transfer(
        @PathVariable problemId: Int,
        fullName: String,
        @RequestBody properties: AdditionalProblemProperties,
        session: HttpSession,
    ) {
        val toastSender = webSocketConnectionKeeper.createSender(session.id) // add problem name
        transferProblemToBacs(toastSender, problemId, properties, true, polygonService, bacsArchiveService)
    }
}

suspend fun downloadProblem(
    toastSender: ToastSender,
    problemId: Int,
    polygonService: PolygonService,
    statementFormat: StatementFormat = StatementFormat.PDF,
): IRProblem {
    try {
        toastSender.send("Выкачиваем задачу из полигона")
        return polygonService.downloadProblem(problemId, true, statementFormat)
    } catch (e: ProblemDownloadingException) {
        val msg = "Не удалось выкачать задачу из полигона: ${e.message}"
        toastSender.send(msg, ToastKind.FAILURE)
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, msg, e)
    }
}

suspend fun transferProblemToBacs(
    toastSender: ToastSender,
    problemId: Int,
    properties: AdditionalProblemProperties,
    isFinalStep: Boolean,
    polygonService: PolygonService,
    bacsArchiveService: BacsArchiveService,
) {
    val irProblem = downloadProblem(toastSender, problemId, polygonService, properties.statementFormat)
    toastSender.send("Задача выкачана из полигона, закидываем в бакс")
    try {
        bacsArchiveService.uploadProblem(irProblem, properties)
    } catch (e: Exception) {
        val msg = "Не удалось закинуть задачу в бакс: ${e.message}"
        toastSender.send(msg, ToastKind.FAILURE)
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, msg, e)
    }
    toastSender.send("Задача закинута в бакс", if (isFinalStep) ToastKind.SUCCESS else ToastKind.INFORMATION)
}