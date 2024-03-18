package io.github.jvmusin.polybacs.api

import io.github.jvmusin.polybacs.api.StatementFormat.PDF

/**
 * Additional problem properties.
 *
 * Used for modifying problem name or time/memory limits when uploading problems to external systems.
 *
 * @param prefix prefix to add to problem name or `null` for no extra prefix.
 * @param suffix suffix to add to problem name or `null` for no extra suffix.
 * @param timeLimitMillis time limit in millis to set or `null` for problem's default time limit.
 * @param memoryLimitMegabytes memory limit in megabytes to set or `null` for problem's default memory limit.
 * @param statementFormat format of the statement, actually `PDF` or `HTML`.
 */
data class AdditionalProblemProperties(
    val name: String,
    val prefix: String? = null, // TODO: Drop nullability; drop whole concept?
    val suffix: String? = null,
    val timeLimitMillis: Int? = null,
    val memoryLimitMegabytes: Int? = null,
    val statementFormat: StatementFormat = PDF
) {
    /** Build problem name prefixing it with [prefix] and suffixing with [suffix] if they are not `null`. */
    fun buildFullName() = "${prefix.orEmpty()}$name${suffix.orEmpty()}"
}
