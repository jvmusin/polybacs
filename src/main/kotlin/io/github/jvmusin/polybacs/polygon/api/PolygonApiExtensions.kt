package io.github.jvmusin.polybacs.polygon.api

import io.github.jvmusin.polybacs.api.StatementFormat
import io.github.jvmusin.polybacs.polygon.exception.response.NoSuchProblemException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.apache.commons.compress.archivers.zip.ZipFile
import org.springframework.util.ConcurrentReferenceHashMap
import java.util.concurrent.ConcurrentHashMap

private val packagesCacheLocks = ConcurrentHashMap<Int, Mutex>()
private val packagesCache = ConcurrentReferenceHashMap<Int, ByteArray>()

private suspend fun PolygonApi.downloadPackageZip(problemId: Int, packageId: Int): ByteArray {
    return packagesCacheLocks.computeIfAbsent(packageId) { Mutex() }.withLock {
        packagesCache.getOrPut(packageId) {
            getPackage(problemId, packageId)
        }
    }
}

suspend fun PolygonApi.getFileFromZipPackage(problemId: Int, packageId: Int, filePath: String): ByteArray? {
    val packageZipBytes = downloadPackageZip(problemId, packageId)
    ZipFile.Builder().setByteArray(packageZipBytes).get().use {
        val entry = it.getEntry(filePath) ?: return null
        return it.getInputStream(entry).readBytes()
    }
}

suspend fun PolygonApi.getStatementRaw(
    problemId: Int,
    packageId: Int,
    format: StatementFormat = StatementFormat.PDF,
    language: String = "russian",
): ByteArray? {
    val formatAsString = format.lowercase
    val path = listOf(
        "statements",
        ".$formatAsString",
        language,
        "problem.$formatAsString",
    ).joinToString("/")
    return getFileFromZipPackage(problemId, packageId, path)
}

/**
 * Returns problem with the given [problemId] from the problem list.
 *
 * @param problemId the problem id to return.
 * @return The problem.
 * @throws NoSuchProblemException if the problem is not found.
 * @see PolygonApi.getProblems
 */
suspend fun PolygonApi.getProblem(problemId: Int) = getProblems().extract().singleOrNull { it.id == problemId }
    ?: throw NoSuchProblemException("There is no problem with id $problemId")

suspend fun PolygonApi.getStatement(problemId: Int, language: String? = null): Pair<String, Statement>? {
    return getStatements(problemId).extract().entries.firstOrNull {
        language == null || it.key == language
    }?.toPair()
}

suspend fun PolygonApi.getLatestPackageId(problemId: Int): Int {
    return getPackages(problemId).extract()
        .filter { it.state == Package.State.READY }
        .maxOf { it.id }
}
