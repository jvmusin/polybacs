package io.github.jvmusin.polybacs.sybon

import io.github.jvmusin.polybacs.api.AdditionalProblemProperties
import io.github.jvmusin.polybacs.bacs.BacsArchiveService
import io.github.jvmusin.polybacs.polygon.PolygonService
import io.github.jvmusin.polybacs.sybon.api.SybonArchiveApi
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlinx.coroutines.delay
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@SpringBootTest
class SybonSpecialCollectionTests(
    bacsArchiveService: BacsArchiveService,
    sybonArchiveApi: SybonArchiveApi,
    polygonService: PolygonService,
) : StringSpec({
    val specialCollectionId = 10023
    val polygonProblemId = 147360
    fun properties(problemName: String) = AdditionalProblemProperties(
        name = problemName,
        suffix = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")
        ) + "-polybacs-test-rustam"
    )

    "Full cycle" {
        val irProblem = polygonService.downloadProblem(polygonProblemId, includeTests = true)
        val fullName = bacsArchiveService.uploadProblem(irProblem, properties(irProblem.name))
        println("Problem full name: $fullName")
        delay(2.minutes) // wait for the problem to appear in the archive
        val sybonProblemId = repeat(3.minutes, 10.seconds) {
            sybonArchiveApi.importProblem(specialCollectionId, fullName)
        }
        println("Sybon problem id: $sybonProblemId")
    }

    "Get special collection" {
        sybonArchiveApi.getCollection(specialCollectionId).problems.shouldNotBeEmpty()
    }
})

private suspend inline fun <T> repeat(timeout: Duration, delay: Duration, action: () -> T): T {
    val start = System.currentTimeMillis()
    while (true) {
        try {
            return action()
        } catch (e: Exception) {
            val now = System.currentTimeMillis()
            if (now - start > timeout.toJavaDuration().toMillis()) throw e
            delay(delay)
        }
    }
}
