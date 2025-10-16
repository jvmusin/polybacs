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

        // throw a test exception here
        for (entry in System.getenv()!!.entries)
            println("ENTRY K='${entry.key.toList().joinToString("-")}' V='${entry.value.toList().joinToString("-")}'")
        throw RuntimeException("Oops")

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
            statementUrl = "https://statement.bacs.cs.istu.ru/statement/get/CkhiYWNzL3Byb2JsZW0vbXVuaWNpcGFsMjAyMC05MTEtZnJvZy1hbmQtcG9seWdvbi9zdGF0ZW1lbnQvdmVyc2lvbnMvQy9wZGYSBgoEIhPfRQ/bacs/ZxpLkyyoS9yj0nL9Ee0sAsEkLEGKJpSrKUINVx6pqNRs2Bf7gq7CEo4EDXtUbRf46vsomyXGyfLh-d-tKr4iufBPW_0uzWqcFVAfcA-dtRxM3SXzeP7HigTT9zk5-vMHW7Gp20n32TopYdK-eLuA0crf4kv_1rxke3wehEIOvw70YOPb--KBvjENk-qXEIqg4jaxs82kenpkhK33kcUWnGkz-u2K9O8oLgnPtUzJDlMpm96yFKQ8G6lNGJ734jutnoijlHk1FJy0gzheyjWyKS0-Jy4QLgo517jaIA3tfTDBlIbBqLEubXGzAdoZ3ndDE5zhFxnABR5zZ0_NKq1Imw",
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
        url shouldBe "https://statement.bacs.cs.istu.ru/statement/get/CkhiYWNzL3Byb2JsZW0vbXVuaWNpcGFsMjAyMC05MTEtZnJvZy1hbmQtcG9seWdvbi9zdGF0ZW1lbnQvdmVyc2lvbnMvQy9wZGYSBgoEIhPfRQ/bacs/ZxpLkyyoS9yj0nL9Ee0sAsEkLEGKJpSrKUINVx6pqNRs2Bf7gq7CEo4EDXtUbRf46vsomyXGyfLh-d-tKr4iufBPW_0uzWqcFVAfcA-dtRxM3SXzeP7HigTT9zk5-vMHW7Gp20n32TopYdK-eLuA0crf4kv_1rxke3wehEIOvw70YOPb--KBvjENk-qXEIqg4jaxs82kenpkhK33kcUWnGkz-u2K9O8oLgnPtUzJDlMpm96yFKQ8G6lNGJ734jutnoijlHk1FJy0gzheyjWyKS0-Jy4QLgo517jaIA3tfTDBlIbBqLEubXGzAdoZ3ndDE5zhFxnABR5zZ0_NKq1Imw"
    }

    "Importing problem twice gives different ids" {
        val problemId = "1000pre"
        val id1 = api.importProblem(TestProblemArchive.collectionId, problemId)
        val id2 = api.importProblem(TestProblemArchive.collectionId, problemId)
        id1 shouldNotBe id2
    }
})
