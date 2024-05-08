package io.github.jvmusin.polybacs.server

import io.github.jvmusin.polybacs.api.AdditionalProblemProperties
import io.github.jvmusin.polybacs.api.ProblemInfo
import io.github.jvmusin.polybacs.api.StatementFormat
import io.github.jvmusin.polybacs.bacs.BacsArchiveService
import io.github.jvmusin.polybacs.ir.IRProblem
import io.github.jvmusin.polybacs.polygon.PolygonService
import io.github.jvmusin.polybacs.polygon.api.toDto
import io.github.jvmusin.polybacs.polygon.exception.downloading.ProblemDownloadingException
import io.github.jvmusin.polybacs.sybon.toZipArchive
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import kotlin.io.path.name

@RestController
@RequestMapping("/api/problems/{problemId}")
class ProblemController(
    private val bacsArchiveService: BacsArchiveService,
    private val polygonService: PolygonService,
    private val statusTracker: StatusTracker
) {
    @GetMapping
    suspend fun getProblem(@PathVariable problemId: Int): ProblemInfo {
        val problemInfo = polygonService.getProblemInfo(problemId)
        val problem = polygonService.getProblems().single { it.id == problemId }
        return problemInfo.toDto(problem)
    }

    @PostMapping("/download")
    suspend fun download(
        @PathVariable problemId: Int,
        fullName: String,
        @RequestBody properties: AdditionalProblemProperties,
        session: HttpSession,
        response: HttpServletResponse,
    ) {
        val updateConsumer = statusTracker.newTrack(problemId, properties.buildFullName(), session.id)
        val irProblem = downloadProblem(updateConsumer, problemId, polygonService, properties.statementFormat)
        val zip = irProblem.toZipArchive(properties)
        updateConsumer.consumeUpdate(
            "The problem is downloaded from Polygon, now saving the archive",
            StatusTrackUpdateSeverity.NEUTRAL
        )
        response.addHeader(
            "Content-Disposition",
            "attachment; filename=${zip.name}"
        )
        response.outputStream.use { zip.toFile().inputStream().copyTo(it) }
        updateConsumer.consumeUpdate("The archive is saved", StatusTrackUpdateSeverity.SUCCESS)
    }

    @PostMapping("/transfer")
    suspend fun transfer(
        @PathVariable problemId: Int,
        fullName: String,
        @RequestBody properties: AdditionalProblemProperties,
        session: HttpSession,
    ) {
        val updateConsumer = statusTracker.newTrack(problemId, properties.buildFullName(), session.id)
        transferProblemToBacs(updateConsumer, problemId, properties, true, polygonService, bacsArchiveService)
    }
}

suspend fun downloadProblem(
    updateConsumer: StatusTrackUpdateConsumer,
    problemId: Int,
    polygonService: PolygonService,
    statementFormat: StatementFormat = StatementFormat.PDF,
): IRProblem {
    try {
        updateConsumer.consumeUpdate("Downloading problem from Polygon", StatusTrackUpdateSeverity.NEUTRAL)
        return polygonService.downloadProblem(problemId, true, statementFormat)
    } catch (e: ProblemDownloadingException) {
        val msg = "Failed to download the problem from Polygon: ${e.message}"
        val logger = LoggerFactory.getLogger("io.github.jvmusin.polybacs.server.downloadProblem")
        logger.warn(msg, e)
        updateConsumer.consumeUpdate(msg, StatusTrackUpdateSeverity.FAILURE)
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, msg, e)
    }
}

suspend fun transferProblemToBacs(
    updateConsumer: StatusTrackUpdateConsumer,
    problemId: Int,
    properties: AdditionalProblemProperties,
    isFinalStep: Boolean,
    polygonService: PolygonService,
    bacsArchiveService: BacsArchiveService,
) {
    val irProblem = downloadProblem(updateConsumer, problemId, polygonService, properties.statementFormat)
    updateConsumer.consumeUpdate("Задача выкачана из полигона, закидываем в бакс", StatusTrackUpdateSeverity.NEUTRAL)
    try {
        bacsArchiveService.uploadProblem(irProblem, properties)
    } catch (e: Exception) {
        val msg = "Не удалось закинуть задачу в бакс: ${e.message}"
        val logger = LoggerFactory.getLogger("io.github.jvmusin.polybacs.server.transferProblemToBacs")
        logger.warn(msg, e)
        updateConsumer.consumeUpdate(msg, StatusTrackUpdateSeverity.FAILURE)
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, msg, e)
    }
    updateConsumer.consumeUpdate(
        "Задача закинута в бакс",
        if (isFinalStep) StatusTrackUpdateSeverity.SUCCESS else StatusTrackUpdateSeverity.NEUTRAL
    )
}