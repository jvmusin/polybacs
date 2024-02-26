package io.github.jvmusin.polybacs.polygon.api

fun Problem.toDto() = io.github.jvmusin.polybacs.api.Problem(id, name, owner, io.github.jvmusin.polybacs.api.ProblemAccessType.valueOf(accessType.name), latestPackage)
fun ProblemInfo.toDto(problem: Problem) = io.github.jvmusin.polybacs.api.ProblemInfo(problem.toDto(), inputFile, outputFile, interactive, timeLimit, memoryLimit)
