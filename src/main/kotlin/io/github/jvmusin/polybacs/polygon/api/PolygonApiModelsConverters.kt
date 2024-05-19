package io.github.jvmusin.polybacs.polygon.api

fun Problem.toDto() = io.github.jvmusin.polybacs.api.Problem(
    id = id,
    name = name,
    owner = owner,
    accessType = io.github.jvmusin.polybacs.api.ProblemAccessType.valueOf(accessType.name),
    latestPackage = latestPackage
)

fun ProblemInfo.toDto(problem: Problem, statementLanguages: Set<String>) = io.github.jvmusin.polybacs.api.ProblemInfo(
    problem = problem.toDto(),
    inputFile = inputFile,
    outputFile = outputFile,
    interactive = interactive,
    timeLimitMillis = timeLimit,
    memoryLimitMegabytes = memoryLimit,
    statementLanguages = statementLanguages
)
