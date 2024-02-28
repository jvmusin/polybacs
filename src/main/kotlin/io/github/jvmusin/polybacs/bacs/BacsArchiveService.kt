package io.github.jvmusin.polybacs.bacs

import io.github.jvmusin.polybacs.api.AdditionalProblemProperties
import io.github.jvmusin.polybacs.ir.IRProblem
import io.github.jvmusin.polybacs.sybon.toZipArchive
import io.github.jvmusin.polybacs.util.RetryPolicy
import kotlinx.coroutines.reactor.awaitSingle
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.toEntity
import java.nio.file.Path
import kotlin.io.path.readBytes

/** Allows communicating to Bacs Archive. */
@Service
class BacsArchiveService(
    private val bacsConfig: BacsConfig,
) {
    private val client = WebClient.builder()
        .defaultHeaders { it.setBasicAuth(bacsConfig.username, bacsConfig.password) }
        .baseUrl("https://archive.bacs.cs.istu.ru/repository")
        .build()

    /**
     * Uploads packed to zip archive problem located at [zip]
     * throwing [BacsProblemUploadException] in case of uploading failure.
     */
    private suspend fun uploadProblem(zip: Path) {
        val body = MultipartBodyBuilder().apply {
            part("_5", "Upload")
            part("archiver_format", "")
            part("archiver_type", "7z")
            part("response", "html")
            part("archive", zip.readBytes())
                .filename(zip.fileName.toString())
                .contentType(MediaType("application", "zip"))
        }.build()

        val response = client.post()
            .uri("/upload")
            .body(BodyInserters.fromMultipartData(body))
            .retrieve()
            .toEntity<String>()
            .awaitSingle()
        if (!response.statusCode.is2xxSuccessful) {
            throw BacsProblemUploadException("Code ${response.statusCode}, Message ${response.body}")
        }

        val content = requireNotNull(response.body)

        if (content.contains("reserved: *$PENDING_IMPORT".toRegex())) return

        val flags = "reserved: (\\S+)".toRegex().findAll(content).map { it.groups[1]!!.value }.toList()
        val extra = when {
            flags.isEmpty() -> "there are no flags"
            else -> "but found flags ${flags.joinToString(",")}"
        }
        throw BacsProblemUploadException("Flag $PENDING_IMPORT not found, $extra")
    }

    /** Retrieves problem status in form if [BacsProblemStatus]. */
    private suspend fun getProblemStatus(problemId: String): BacsProblemStatus {
        val body = MultipartBodyBuilder().apply {
            part("response", "html")
            part("ids", problemId)
            part("_4", "Get status")
        }.build()

        val content = client.post()
            .uri("/status")
            .body(BodyInserters.fromMultipartData(body))
            .retrieve()
            .awaitBody<String>()

        val row = Jsoup.parse(content).body()
            .getElementsByTag("table")[0]
            .getElementsByTag("tbody")[0]
            .getElementsByTag("tr")[1]
            .getElementsByTag("td")
            .map { it.text().trim() }

        if (row.size == 2) {
            return BacsProblemStatus(row[1], emptyList(), "")
        }

        val flagRegex = "flag\\{(?<name>.*?):(?<value>.*?)}".toRegex()

        val name = row[1]
        val revision = row[3]
        val flagsRaw = row[2].replace("\\s".toRegex(), "")
        val flags = flagRegex.findAll(flagsRaw)
            .map { "${it.groups["name"]!!.value}: ${it.groups["value"]!!.value}" }
            .toList()
        return BacsProblemStatus(name, flags, revision)
    }

    /** Retrieves problem state in form if [BacsProblemState]. */
    suspend fun getProblemState(problemId: String): BacsProblemState {
        return try {
            getProblemStatus(problemId).state
        } catch (e: Exception) {
            LoggerFactory.getLogger(javaClass).warn("Failed to get problem status", e)
            BacsProblemState.UNKNOWN
        }
    }

    /**
     * Waits for some time until problem with the given [problemId] is imported.
     *
     * Uses [retryPolicy] to schedule requests.
     */
    private suspend fun waitTillProblemIsImported(
        problemId: String,
        retryPolicy: RetryPolicy = RetryPolicy(),
    ): BacsProblemState {
        return retryPolicy.evalWhileFails({ it != BacsProblemState.PENDING_IMPORT }) {
            getProblemStatus(problemId).state
        }
    }

    /** Uploads [problem] to Bacs archive with extra [properties]. */
    suspend fun uploadProblem(
        problem: IRProblem,
        properties: AdditionalProblemProperties = AdditionalProblemProperties.defaultProperties,
    ): String {
        val zip = problem.toZipArchive(properties)
        uploadProblem(zip)
        val fullName = properties.buildFullName(problem.name)
        val state = waitTillProblemIsImported(fullName)
        if (state != BacsProblemState.IMPORTED)
            throw BacsProblemUploadException("Задача $fullName не импортирована, статус $state")
        return fullName
    }

    private companion object {
        const val PENDING_IMPORT = "PENDING_IMPORT"
    }
}
