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

private suspend inline fun <T> PolygonApi.usePackageZip(problemId: Int, packageId: Int, block: (zip: ZipFile) -> T): T {
    val packageZipBytes = downloadPackageZip(problemId, packageId)
    return ZipFile.Builder().setByteArray(packageZipBytes).get().use(block)
}

suspend fun PolygonApi.getFileFromZipPackage(problemId: Int, packageId: Int, filePath: String): ByteArray? {
    return usePackageZip(problemId, packageId) { zip ->
        zip.getEntry(filePath)?.let { entry ->
            zip.getInputStream(entry).readBytes()
        }
    }
}

private suspend fun PolygonApi.getFilesFromZipPackage(
    problemId: Int,
    packageId: Int,
    filesPath: String
): Map<String, ByteArray> {
    val prefix = "$filesPath/"
    return usePackageZip(problemId, packageId) { zip ->
        zip.entries.asSequence().filter { it.name.startsWith(prefix) && !it.isDirectory }.associate { entry ->
            entry.name.removePrefix(prefix) to zip.getInputStream(entry).readBytes()
        }
    }
}

suspend fun PolygonApi.getSolutionsFromZipPackage(
    problemId: Int,
    packageId: Int
): Map<String, PolygonSolutionWithDescription> {
    val solutionNames = getSolutions(problemId).extract().map { it.name }
    return usePackageZip(problemId, packageId) { zip ->
        solutionNames.associate { name ->
            val solution = zip.getInputStream(zip.getEntry("solutions/$name")).readBytes().decodeToString()
            val description = zip.getInputStream(zip.getEntry("solutions/$name.desc")).readBytes().decodeToString()
            name to PolygonSolutionWithDescription(name, solution, description)
        }
    }
}

suspend fun PolygonApi.getProblemXmlFromZipPackage(
    problemId: Int,
    packageId: Int
): ByteArray {
    return usePackageZip(problemId, packageId) { zip ->
        zip.getInputStream(zip.getEntry("problem.xml")).readBytes()
    }
}

data class PolygonSolutionWithDescription(
    val name: String,
    val solution: String,
    val description: String,
)

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
