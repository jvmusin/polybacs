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
        zip.readFile(filePath)
        zip.getEntry(filePath)?.let { entry ->
            zip.getInputStream(entry).readBytes()
        }
    }
}

suspend fun PolygonApi.getFilesFromZipPackage(
    problemId: Int,
    packageId: Int,
    filesPath: String,
    filter: (name: String) -> Boolean = { true }
): Map<String, ByteArray> {
    val prefix = "$filesPath/"
    return usePackageZip(problemId, packageId) { zip ->
        zip.entries.asSequence()
            .filter { !it.isDirectory && it.name.startsWith(prefix) && filter(it.name.removePrefix(prefix)) }
            .associate { it.name.removePrefix(prefix) to zip.getInputStream(it).readBytes() }
    }
}

private fun ZipFile.readFile(name: String): ByteArray? {
    return getEntry(name)?.let { entry ->
        getInputStream(entry).use {
            it.readBytes()
        }
    }
}

suspend fun PolygonApi.getSolutionsFromZipPackage(
    problemId: Int,
    packageId: Int
): Map<String, PolygonSolutionWithDescription> {
    val solutionNames = getSolutions(problemId).extract().map { it.name }
    return usePackageZip(problemId, packageId) { zip ->
        solutionNames.associateWith { name ->
            val solution = requireNotNull(zip.readFile("solutions/$name")) { "Solution $name not found" }
            val description = requireNotNull(zip.readFile("solutions/$name.desc")) {
                "Solution description $name not found"
            }
            PolygonSolutionWithDescription(name, solution.decodeToString(), description.decodeToString())
        }
    }
}

data class PolygonSolutionWithDescription(
    val name: String,
    val solution: String,
    val description: String,
)

suspend fun PolygonApi.getStatementFiles(
    problemId: Int,
    packageId: Int,
    format: StatementFormat = StatementFormat.PDF,
    language: String = "russian",
): Map<String, ByteArray> {
    val formatString = format.lowercase
    val path = listOf(
        "statements",
        ".$formatString",
        language,
    ).joinToString("/")

    return getFilesFromZipPackage(problemId, packageId, path)
}

suspend fun PolygonApi.getTutorialRaw(
    problemId: Int,
    packageId: Int,
    format: StatementFormat,
    language: String,
): ByteArray? {
    val formatString = format.lowercase
    val path = listOf(
        "statements",
        ".$formatString",
        language,
        "tutorial.$formatString",
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

suspend fun PolygonApi.getStatement(problemId: Int, language: String? = null): Statement? {
    return getStatements(problemId).extract().let {
        if (language == null) it.values.first() // return any statement
        else it[language]
    }
}

suspend fun PolygonApi.getLatestPackageId(problemId: Int): Int {
    return getPackages(problemId).extract()
        .filter { it.state == Package.State.READY }
        .maxOf { it.id }
}
