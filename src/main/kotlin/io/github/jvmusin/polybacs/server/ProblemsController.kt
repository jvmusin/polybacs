package io.github.jvmusin.polybacs.server

import io.github.jvmusin.polybacs.api.NameAvailability
import io.github.jvmusin.polybacs.api.Problem
import io.github.jvmusin.polybacs.bacs.BacsArchiveService
import io.github.jvmusin.polybacs.bacs.BacsProblemState
import io.github.jvmusin.polybacs.polygon.PolygonService
import io.github.jvmusin.polybacs.polygon.api.toDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/problems")
class ProblemsController(
    private val polygonService: PolygonService,
    private val bacsArchiveService: BacsArchiveService,
) {
    @GetMapping
    suspend fun getProblems(): List<Problem> {
        val problems = polygonService.getProblems()
        return problems.map { it.toDto() }
    }

    @GetMapping("/nameAvailability")
    suspend fun getNameAvailability(name: String): NameAvailability {
        return when (bacsArchiveService.getProblemState(name)) {
            BacsProblemState.NOT_FOUND -> NameAvailability.AVAILABLE
            BacsProblemState.IMPORTED, BacsProblemState.PENDING_IMPORT -> NameAvailability.TAKEN
            BacsProblemState.UNKNOWN -> NameAvailability.CHECK_FAILED
        }
    }
}