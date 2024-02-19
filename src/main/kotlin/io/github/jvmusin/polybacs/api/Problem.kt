package io.github.jvmusin.polybacs.api

data class Problem(
    val id: Int,
    val name: String,
    val owner: String,
    val accessType: ProblemAccessType,
    val latestPackage: Int?
)
