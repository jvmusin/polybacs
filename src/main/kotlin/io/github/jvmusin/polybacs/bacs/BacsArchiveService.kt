package io.github.jvmusin.polybacs.bacs

import io.github.jvmusin.polybacs.api.AdditionalProblemProperties
import io.github.jvmusin.polybacs.ir.IRProblem
import io.github.jvmusin.polybacs.sybon.toZipArchive
import io.github.jvmusin.polybacs.util.RetryPolicy
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.IOException
import java.nio.file.Path

/** Allows communicating to Bacs Archive. */
@Service
class BacsArchiveService(
    private val bacsConfig: BacsConfig,
) {
    // TODO: Replace with Spring client
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", Credentials.basic(bacsConfig.username, bacsConfig.password))
                .build()
            chain.proceed(newRequest)
        }
        .build()

    /**
     * Uploads packed to zip archive problem located at [zip]
     * throwing [BacsProblemUploadException] in case of uploading failure.
     */
    private suspend fun uploadProblem(zip: Path) {
        val archiveFile = zip.toFile()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("archiver_format", "")
            .addFormDataPart("archiver_type", "7z")
            .addFormDataPart("response", "html")
            .addFormDataPart(
                "archive",
                archiveFile.name,
                archiveFile.asRequestBody("application/zip".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url("$BASE_URL/upload")
            .post(requestBody)
            .build()

        val response = try {
            client.newCall(request).execute()
        } catch (e: Exception) {
            throw BacsProblemUploadException("Error uploading the problem: ${e.message}")
        }

        if (!response.isSuccessful) {
            throw BacsProblemUploadException("Error uploading the problem: ${response.code}")
        }

        val content = requireNotNull(response.body) {
            "Empty response body"
        }.string()

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
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("response", "html")
            .addFormDataPart("ids", problemId)
            .build()

        val request = Request.Builder()
            .url("$BASE_URL/status")
            .post(requestBody)
            .build()

        val response = try {
            client.newCall(request).execute()
        } catch (e: IOException) {
            throw BacsProblemUploadException("Failed to get status: ${e.message}")
        }

        if (!response.isSuccessful) {
            throw BacsProblemUploadException("Unexpected response code: ${response.code}")
        }

        val content = requireNotNull(response.body) {
            "Empty response body"
        }.string()

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
        properties: AdditionalProblemProperties,
    ): String {
        val zip = problem.toZipArchive(properties)
        uploadProblem(zip)
        val fullName = properties.buildFullName()
        val state = waitTillProblemIsImported(fullName)
        if (state != BacsProblemState.IMPORTED)
            throw BacsProblemUploadException("Задача $fullName не импортирована, статус $state")
        return fullName
    }

    private companion object {
        const val PENDING_IMPORT = "PENDING_IMPORT"
        const val BASE_URL = "https://archive.bacs.cs.istu.ru/repository"
    }
}
