@file:Suppress("unused")

package io.github.jvmusin.polybacs.polygon.api

data class Problem(
    val id: Int,
    val owner: String,
    val name: String,
    val deleted: Boolean,
    val favourite: Boolean,
    val accessType: AccessType,
    val revision: Int,
    val latestPackage: Int? = null,
    val modified: Boolean,
) {
    enum class AccessType {
        READ,
        WRITE,
        OWNER
    }
}

/**
 * Problem info.
 *
 * @property inputFile Input file name or **stdin** if no input file is used.
 * @property outputFile Output file name or **stdout** if no output file is used.
 * @property interactive Whether the problem is interactive or not.
 * @property timeLimit Time limit in milliseconds.
 * @property memoryLimit Memory limit in megabytes.
 */
data class ProblemInfo(
    val inputFile: String,
    val outputFile: String,
    val interactive: Boolean,
    val timeLimit: Int,
    val memoryLimit: Int,
)

data class Statement(
    val encoding: String,
    val name: String,
    val legend: String,
    val input: String,
    val output: String,
    val scoring: String? = null,
    val notes: String,
    val tutorial: String,
)

data class File(
    val name: String,
    val modificationTimeSeconds: Long,
    val length: Long,
    val sourceType: String? = null,
    val resourceAdvancedProperties: ResourceAdvancedProperties? = null,
) {
    enum class Type {
        RESOURCE,
        SOURCE,
        AUX;

        override fun toString() = super.toString().lowercase()
    }
}

data class ResourceAdvancedProperties(
    val forTypes: String,
    val main: String,
    val stages: List<StageType>,
    val assets: List<AssetType>,
) {
    enum class StageType {
        COMPILE,
        RUN
    }

    enum class AssetType {
        VALIDATOR,
        INTERACTOR,
        CHECKER,
        SOLUTION
    }
}

data class Solution(
    val name: String,
    val modificationTimeSeconds: Int,
    val length: Int,
    val sourceType: String,
    val tag: String,
) {
    val isMain get() = tag == "MA"
}

data class PolygonTest(
    val index: Int,
    val manual: Boolean,
    val input: String? = null,
    val description: String? = null,
    val useInStatements: Boolean,
    val scriptLine: String? = null,
    val group: String? = null,
    val points: Double? = null,
    val inputForStatement: String? = null,
    val outputForStatement: String? = null,
    val verifyInputOutputForStatements: Boolean? = null,
)

data class TestGroup(
    val name: String,
    val pointsPolicy: PointsPolicyType,
    val feedbackPolicy: String, // add enums
    val dependencies: List<String>,
) {
    enum class PointsPolicyType {
        COMPLETE_GROUP,
        EACH_TEST
    }
}

data class Package(
    val id: Int,
    val creationTimeSeconds: Long,
    val state: State,
    val comment: String,
    val revision: Int,
    val type: String, // https://github.com/Codeforces/polygon-issue-tracking/issues/498
) {
    enum class State {
        PENDING,
        RUNNING,
        READY,
        FAILED
    }
}
