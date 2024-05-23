package io.github.jvmusin.polybacs.ir

import io.github.jvmusin.polybacs.api.StatementFormat

data class IRProblem(
    val name: String,
    val owner: String,
    val statement: IRStatement,
    val limits: IRLimits,
    val tests: List<IRTest>?,
    val groups: List<IRTestGroup>?,
    val checker: IRChecker,
    val solutions: List<IRSolution>,
    val miscFiles: List<IRMiscFile>,
    val revision: Int,
)

interface IRFile {
    val destination: String
    val content: ByteArray
}

data class IRStatementExtraFile(override val destination: String, override val content: ByteArray) : IRFile {
    override fun equals(other: Any?) = throw NotImplementedError()
    override fun hashCode() = throw NotImplementedError()
}

data class IRMiscFile(override val destination: String, override val content: ByteArray) : IRFile {
    override fun equals(other: Any?) = throw NotImplementedError()
    override fun hashCode() = throw NotImplementedError()
}

data class IRStatement(
    val name: String,
    val files: List<IRStatementExtraFile>,
    val format: StatementFormat = StatementFormat.PDF
)

data class IRTest(
    val index: Int,
    val isSample: Boolean,
    val input: ByteArray,
    val output: ByteArray,
    val points: Int?,
    val groupName: String?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IRTest

        if (index != other.index) return false
        if (isSample != other.isSample) return false
        if (!input.contentEquals(other.input)) return false
        if (!output.contentEquals(other.output)) return false
        if (points != other.points) return false
        if (groupName != other.groupName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + isSample.hashCode()
        result = 31 * result + input.contentHashCode()
        result = 31 * result + output.contentHashCode()
        result = 31 * result + (points ?: 0)
        result = 31 * result + (groupName?.hashCode() ?: 0)
        return result
    }
}

data class IRChecker(val name: String, val content: String)
data class IRLimits(val timeLimitMillis: Int, val memoryLimitMegabytes: Int)
data class IRSolution(
    val name: String,
    val verdict: IRVerdict,
    val isMain: Boolean,
    val language: IRLanguage,
    val content: String,
    val description: String,
)

enum class IRVerdict {
    OK,
    WRONG_ANSWER,
    TIME_LIMIT_EXCEEDED,
    MEMORY_LIMIT_EXCEEDED,
    PRESENTATION_ERROR,
    INCORRECT,
    OTHER
}

enum class IRLanguage(val fullName: String) {
    CPP("C++"),
    JAVA("Java"),
    KOTLIN("Kotlin"),
    PYTHON2("Python 2"),
    PYTHON3("Python 3"),
    OTHER("Other")
}

/**
 * IR test group points policy.
 *
 * Used to understand how to score solutions with test groups.
 */
enum class IRTestGroupPointsPolicy {
    /**
     * Give scores only when all tests in the group are passed.
     */
    COMPLETE_GROUP,

    /**
     * Give scores for each passed test in the group.
     */
    EACH_TEST,

    /**
     * Do not give any points for passing tests in this group.
     *
     * Intended to use in samples group.
     */
    SAMPLES
}

data class IRTestGroup(
    val name: String,
    val pointsPolicy: IRTestGroupPointsPolicy,
    val testIndices: List<Int>,
    val points: Int?,
)
