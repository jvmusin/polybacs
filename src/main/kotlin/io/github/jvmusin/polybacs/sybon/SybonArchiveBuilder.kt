package io.github.jvmusin.polybacs.sybon

import io.github.jvmusin.polybacs.api.AdditionalProblemProperties
import io.github.jvmusin.polybacs.ir.IRProblem
import io.github.jvmusin.polybacs.util.toZipArchive
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.io.path.createParentDirectories
import kotlin.io.path.writeBytes
import kotlin.io.path.writeText

/**
 * Packs `this` [IRProblem] into a zip-archive using extra [properties].
 *
 * @return file where the zip is located.
 */
fun IRProblem.toZipArchive(properties: AdditionalProblemProperties): Path {
    val fullName = properties.buildFullName()
    val destinationPath = Paths.get(
        "sybon-packages",
        "$fullName-${UUID.randomUUID()}",
        fullName
    )
    check(destinationPath.toFile().deleteRecursively()) { "Destination directory was not deleted" }

    val checkerPath = destinationPath.resolve("checker")
    val miscPath = destinationPath.resolve("misc")
    val solutionPath = miscPath.resolve("solution")
    val statementPath = destinationPath.resolve("statement")
    val testsPath = destinationPath.resolve("tests")
    val materialsPath = miscPath.resolve("materials")
    for (path in arrayOf(destinationPath, checkerPath, miscPath, solutionPath, statementPath, testsPath, materialsPath))
        path.createDirectories()

    fun writeConfig() {
        val timeLimitMillis = properties.timeLimitMillis ?: limits.timeLimitMillis
        val memoryLimitMegabytes = properties.memoryLimitMegabytes ?: limits.memoryLimitMegabytes
        destinationPath.resolve("config.ini").writeText(
            """
                [info]
                name = ${statement.name}
                maintainers = ${setOf(owner, "Musin").joinToString(" ")}
                
                [resource_limits]
                time = ${"%.2f".format(Locale.ENGLISH, timeLimitMillis / 1000.0)}s
                memory = ${memoryLimitMegabytes}MiB
                
                [tests]
                group_pre = ${requireNotNull(tests).filter { it.isSample }.joinToString(" ") { it.index.toString() }}
                score_pre = 0
                continue_condition_pre = WHILE_OK
                score = 100
                continue_condition = ALWAYS
            """.trimIndent()
        )
    }

    fun writeFormat() {
        destinationPath.resolve("format").writeText("bacs/problem/single#simple0")
    }

    fun writeChecker() {
        checkerPath.resolve("check.cpp").writeText(checker.content)
        checkerPath.resolve("config.ini").writeText(
            """
                [build]
                builder = single
                source = check.cpp
                libs = testlib.googlecode.com-0.9.12
    
                [utility]
                call = in_out_hint
                return = testlib
            """.trimIndent()
        )
    }

    fun writeSolutions() {
        solutions.forEach { s ->
            solutionPath.resolve(s.name).writeText(s.content)
            solutionPath.resolve(s.name + ".desc").writeText(s.description)
        }
    }

    fun writeStatement() {
        val statementFileName = "problem.${statement.format.lowercase}"
        statement.files.forEach { statementPath.resolve(it.destination).writeBytes(it.content) }
        statementPath.resolve("pdf.ini").writeText(
            """
                [info]
                language = C

                [build]
                builder = copy
                source = $statementFileName
            """.trimIndent()
        )
    }

    fun writeTests() {
        requireNotNull(tests)
        fun writeTest(index: Int, type: String, content: ByteArray) = testsPath.resolve("$index.$type").writeBytes(content)
        for (t in tests) writeTest(t.index, "in", t.input)
        for (t in tests) writeTest(t.index, "out", t.output)
    }

    fun writeMiscFiles() {
        for (file in miscFiles) {
            val destination = miscPath.resolve(file.destination)
            destination.createParentDirectories()
            destination.writeBytes(file.content)
        }
    }

    fun writeContacts() {
        miscPath.resolve("contacts.txt").writeText(ContactsGenerator.generate(this))
    }

    writeConfig()
    writeFormat()
    writeChecker()
    writeSolutions()
    writeStatement()
    writeTests()
    writeMiscFiles()
    writeContacts()

    val parent = destinationPath.parent
    val zipPath = Paths.get("ready", "${parent.fileName}.zip")
    parent.toZipArchive(zipPath)
    return zipPath
}
