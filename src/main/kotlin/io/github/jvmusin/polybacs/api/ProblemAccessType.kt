package io.github.jvmusin.polybacs.api

enum class ProblemAccessType(val isSufficient: Boolean) {
    READ(false),
    WRITE(true),
    OWNER(true);

    val notSufficient = !isSufficient
}
