package io.github.jvmusin.polybacs.api

data class ProblemInfo(
    val problem: Problem,
    val inputFile: String,
    val outputFile: String,
    val interactive: Boolean,
    val timeLimitMillis: Int,
    val memoryLimitMegabytes: Int,
)
