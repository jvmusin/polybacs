package io.github.jvmusin.polybacs.sybon

import io.github.jvmusin.polybacs.api.AdditionalProblemProperties
import io.github.jvmusin.polybacs.bacs.BacsArchiveService
import io.github.jvmusin.polybacs.polygon.PolygonService
import io.github.jvmusin.polybacs.sybon.api.SybonArchiveApi
import io.kotest.core.spec.style.StringSpec
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime

@SpringBootTest
class SybonSpecialCollectionTests(
    bacsArchiveService: BacsArchiveService,
    sybonArchiveApi: SybonArchiveApi,
    polygonService: PolygonService,
) : StringSpec({
    val specialCollectionId = 10023
    val polygonProblemId = 147360
    val properties = AdditionalProblemProperties(suffix = LocalDateTime.now().run { "-$hour-$minute" })

    "!Full cycle" {
        val irProblem = polygonService.downloadProblem(polygonProblemId)
        val fullName = bacsArchiveService.uploadProblem(irProblem, properties)
        val sybonProblemId = sybonArchiveApi.importProblem(specialCollectionId, fullName)
        println(fullName)
        println(sybonProblemId)
        println(sybonArchiveApi.getCollection(specialCollectionId))
    }

    "Print special collection" {
        println(sybonArchiveApi.getCollection(specialCollectionId))
    }
})
