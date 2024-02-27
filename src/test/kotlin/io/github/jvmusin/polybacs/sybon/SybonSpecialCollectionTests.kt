package io.github.jvmusin.polybacs.sybon

import io.github.jvmusin.polybacs.api.AdditionalProblemProperties
import io.github.jvmusin.polybacs.bacs.BacsArchiveService
import io.github.jvmusin.polybacs.polygon.PolygonService
import io.github.jvmusin.polybacs.sybon.api.SybonArchiveApi
import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.delay
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.minutes

@SpringBootTest
class SybonSpecialCollectionTests(
    bacsArchiveService: BacsArchiveService,
    sybonArchiveApi: SybonArchiveApi,
    polygonService: PolygonService,
) : StringSpec({
    val specialCollectionId = 10023
    val polygonProblemId = 147360
    val properties = AdditionalProblemProperties(suffix = LocalDateTime.now().run { "-$hour-$minute-test-rustam" })

    "Full cycle" {
        val irProblem = polygonService.downloadProblem(polygonProblemId, includeTests = true)
        val fullName = bacsArchiveService.uploadProblem(irProblem, properties)
        delay(3.minutes) // wait until the problem is indexed by BACS
        val sybonProblemId = sybonArchiveApi.importProblem(specialCollectionId, fullName)
        println(fullName)
        println(sybonProblemId)
        println(sybonArchiveApi.getCollection(specialCollectionId))
    }

    "Print special collection" {
        println(sybonArchiveApi.getCollection(specialCollectionId))
    }
})
