package io.github.jvmusin.polybacs.sybon

import io.github.jvmusin.polybacs.sybon.api.ResourceLimits
import io.github.jvmusin.polybacs.sybon.api.SybonArchiveApi
import io.github.jvmusin.polybacs.sybon.api.SybonCollection
import io.github.jvmusin.polybacs.sybon.api.SybonProblem
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SybonArchiveApiTests(api: SybonArchiveApi) : StringSpec({
    "getCollections should return many collections" {
        val expected = SybonCollection(
            id = 1,
            name = "Global",
            description = "Only Admins",
            problems = emptyList(),
            problemsCount = 0
        )
        val collections = api.getCollections()
        println(collections.first { it.name == "Polybacs Testing" })
        collections shouldHaveAtLeastSize 17
        val collection = collections.first()
        collection.problemsCount shouldBeGreaterThanOrEqual 7022
        collection.copy(problemsCount = 0) shouldBe expected
    }

    "getCollection with id 1 should return 'Only Admins' collection" {
        val collectionId = 1
        val expected = SybonCollection(
            id = collectionId,
            name = "Global",
            description = "Only Admins",
            problems = emptyList(),
            problemsCount = 0
        )
        val collection = api.getCollection(collectionId)
        collection.copy(problems = emptyList(), problemsCount = 0) shouldBe expected
        collection.problems shouldHaveSize collection.problemsCount
        collection.problems shouldHaveAtLeastSize 7022
    }

    "getCollection for test collection returns the test collection" {
        val expected = SybonCollection(
            id = TestProblemArchive.collectionId,
            name = "Polybacs Testing",
            description = "Polybacs tests the problems here",
            problems = emptyList(),
            problemsCount = 0
        )
        val result = api.getCollection(TestProblemArchive.collectionId)
        result.copy(problemsCount = 0, problems = emptyList()) shouldBe expected
    }

    "getProblem should return the correct problem" {
        val problemId = 72147
        val expected = SybonProblem(
            id = problemId,
            name = "Лягушка и многоугольник",
            author = "Musin",
            format = "pdf",
            statementUrl = "http://statement.bacs.cs.istu.ru/statement/get/CkhiYWNzL3Byb2JsZW0vbXVuaWNpcGFsMjAyMC05MTEtZnJvZy1hbmQtcG9seWdvbi9zdGF0ZW1lbnQvdmVyc2lvbnMvQy9wZGYSBgoEMc68zg/bacs/RRtTY4-b81yftuSQdorVUh5w7Z8m-bDUtKdT172cGv9dSMFpF95pNdlbElEyfpMPVmgnokw-yaNEJ2tFgPvCYUvrQaxyYdpvMcFc-MklPkxvooZWcdDm3Xvu4MbD8bOmyn1JwzrydffH1vzBs3CaA-AzO89PP4Di1mu1-IScfN4-JDNN4TIe9RqdtJUGKc61XX96Zh7sVmukRBeiUUILcc3Eem3HPGm9xrKDQcxexSM9B0heJxWqVvKbGv11m1ojTdU-fO5Vi1oOif9WCMGU47oCV6upmk57_Fq-HyuQt1b2s5xGyZ1ToFSwicDF4Z9MlqsMPhOPMuWV9KCr4_GC7A",
            collectionId = 1,
            testsCount = 62,
            pretests = emptyList(),
            inputFileName = "STDIN",
            outputFileName = "STDOUT",
            internalProblemId = "municipal2020-911-frog-and-polygon",
            resourceLimits = ResourceLimits(
                timeLimitMillis = 1000,
                memoryLimitBytes = 268435456
            )
        )
        val problem = api.getProblem(problemId)
        problem shouldBe expected
    }

    "getProblemStatementUrl should return the correct url" {
        val problemId = 72147
        val url = api.getProblemStatementUrl(problemId)
        url shouldBe "http://statement.bacs.cs.istu.ru/statement/get/CkhiYWNzL3Byb2JsZW0vbXVuaWNpcGFsMjAyMC05MTEtZnJvZy1hbmQtcG9seWdvbi9zdGF0ZW1lbnQvdmVyc2lvbnMvQy9wZGYSBgoEMc68zg/bacs/RRtTY4-b81yftuSQdorVUh5w7Z8m-bDUtKdT172cGv9dSMFpF95pNdlbElEyfpMPVmgnokw-yaNEJ2tFgPvCYUvrQaxyYdpvMcFc-MklPkxvooZWcdDm3Xvu4MbD8bOmyn1JwzrydffH1vzBs3CaA-AzO89PP4Di1mu1-IScfN4-JDNN4TIe9RqdtJUGKc61XX96Zh7sVmukRBeiUUILcc3Eem3HPGm9xrKDQcxexSM9B0heJxWqVvKbGv11m1ojTdU-fO5Vi1oOif9WCMGU47oCV6upmk57_Fq-HyuQt1b2s5xGyZ1ToFSwicDF4Z9MlqsMPhOPMuWV9KCr4_GC7A"
    }

    "Importing problem twice gives different ids" {
        val problemId = "1000pre"
        val id1 = api.importProblem(TestProblemArchive.collectionId, problemId)
        val id2 = api.importProblem(TestProblemArchive.collectionId, problemId)
        id1 shouldNotBe id2
    }
})
