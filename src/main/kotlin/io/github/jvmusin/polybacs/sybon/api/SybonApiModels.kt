@file:Suppress("unused")

package io.github.jvmusin.polybacs.sybon.api

data class SybonCollection(
    val id: Int,
    val name: String,
    val description: String,
    val problems: List<SybonProblem>,
    val problemsCount: Int,
)

data class SybonProblem(
    val id: Int,
    val name: String,
    val author: String,
    val format: String,
    val statementUrl: String,
    val collectionId: Int,
    val testsCount: Int,
    val pretests: List<Test>,
    val inputFileName: String,
    val outputFileName: String,
    val internalProblemId: String,
    val resourceLimits: ResourceLimits,
)

data class Test(
    val id: String,
    val input: String,
    val output: String,
)

data class ResourceLimits(
    val timeLimitMillis: Int,
    val memoryLimitBytes: Int,
)

data class SybonCompiler(
    val id: Int,
    val type: Type,
    val name: String,
    val description: String,
    val args: String,
    val timeLimitMillis: Int,
    val memoryLimitBytes: Int,
    val numberOfProcesses: Int,
    val outputLimitBytes: Int,
    val realTimeLimitMillis: Int,
) {
    @Suppress("EnumEntryName")
    enum class Type {
        gcc,
        mono,
        fpc,
        python,
        java,
        dotnet,
        kotlin,
    }
}

data class SybonSubmitSolution(
    val compilerId: Int,
    val problemId: Int,
    val solution: String,
    val solutionFileType: FileType = FileType.Text,
    val pretestsOnly: Boolean = false,
    val continueCondition: ContinueCondition = ContinueCondition.WhileOk,
) {
    enum class FileType {
        Text,
        Zip
    }

    enum class ContinueCondition {
        Default,
        WhileOk,
        Always
    }
}

data class SybonSubmissionResult(
    val id: Int,
    val buildResult: BuildResult,
    val testGroupResults: List<TestGroupResult>,
) {
    data class BuildResult(
        val status: Status,
        val output: String,
    ) {
        enum class Status {
            OK,
            FAILED,
            PENDING,
            SERVER_ERROR
        }
    }

    data class TestGroupResult(
        val internalId: String,
        val executed: Boolean,
        val testResults: List<TestResult>,
    ) {
        data class TestResult(
            val status: Status,
            val judgeMessage: String? = null,
            val resourceUsage: ResourceUsage,
            val input: String? = null,
            val actualResult: String? = null,
            val expectedResult: String? = null,
        ) {
            enum class Status {
                OK,
                WRONG_ANSWER,
                PRESENTATION_ERROR,
                QUERIES_LIMIT_EXCEEDED,
                INCORRECT_REQUEST,
                INSUFFICIENT_DATA,
                EXCESS_DATA,
                OUTPUT_LIMIT_EXCEEDED,
                TERMINATION_REAL_TIME_LIMIT_EXCEEDED,
                ABNORMAL_EXIT,
                MEMORY_LIMIT_EXCEEDED,
                TIME_LIMIT_EXCEEDED,
                REAL_TIME_LIMIT_EXCEEDED,
                TERMINATED_BY_SYSTEM,
                CUSTOM_FAILURE,
                FAIL_TEST,
                FAILED,
                SKIPPED
            }

            data class ResourceUsage(
                val timeUsageMillis: Int,
                val memoryUsageBytes: Int,
            )
        }
    }
}
