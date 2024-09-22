@file:Suppress("MemberVisibilityCanBePrivate")

package io.github.jvmusin.polybacs.sybon

import io.github.jvmusin.polybacs.sybon.api.SybonCompiler

/**
 * Sybon compilers.
 *
 * Contains all known Sybon compilers in one place.
 */
object SybonCompilers {
    val C = SybonCompiler(
        id = 1,
        type = SybonCompiler.Type.gcc,
        name = "C",
        description = "C11",
        args = "lang=c,optimize=2,std=c11,fno-stack-limit,lm",
        timeLimitMillis = 60000,
        memoryLimitBytes = 1610612736,
        numberOfProcesses = 1000,
        outputLimitBytes = 536870912,
        realTimeLimitMillis = 120000
    )
    val CPP = SybonCompiler(
        id = 2,
        type = SybonCompiler.Type.gcc,
        name = "C++",
        description = "C++11",
        args = "lang=c++,optimize=2,std=c++11,fno-stack-limit",
        timeLimitMillis = 60000,
        memoryLimitBytes = 1610612736,
        numberOfProcesses = 1000,
        outputLimitBytes = 536870912,
        realTimeLimitMillis = 120000
    )
    val CSHARP_MONO = SybonCompiler(
        id = 3,
        type = SybonCompiler.Type.mono,
        name = "C# mono",
        description = "mono",
        args = "lang=d",
        timeLimitMillis = 60000,
        memoryLimitBytes = 1610612736,
        numberOfProcesses = 1000,
        outputLimitBytes = 536870912,
        realTimeLimitMillis = 120000
    )
    val DELPHI = SybonCompiler(
        id = 4,
        type = SybonCompiler.Type.fpc,
        name = "Delphi",
        description = "FreePascal PascalABC",
        args = "lang=delphi,optimize=2",
        timeLimitMillis = 60000,
        memoryLimitBytes = 1610612736,
        numberOfProcesses = 1000,
        outputLimitBytes = 536870912,
        realTimeLimitMillis = 120000
    )
    val PASCAL = SybonCompiler(
        id = 5,
        type = SybonCompiler.Type.fpc,
        name = "Pascal",
        description = "FreePascal",
        args = "lang=fpc,optimize=2",
        timeLimitMillis = 60000,
        memoryLimitBytes = 1610612736,
        numberOfProcesses = 1000,
        outputLimitBytes = 536870912,
        realTimeLimitMillis = 120000
    )
    val PYTHON2 = SybonCompiler(
        id = 6,
        type = SybonCompiler.Type.python,
        name = "Python 2",
        description = "Python 2",
        args = "lang=2",
        timeLimitMillis = 60000,
        memoryLimitBytes = 1610612736,
        numberOfProcesses = 1000,
        outputLimitBytes = 536870912,
        realTimeLimitMillis = 120000
    )
    val PYTHON3 = SybonCompiler(
        id = 7,
        type = SybonCompiler.Type.python,
        name = "Python 3",
        description = "Python 3",
        args = "lang=3",
        timeLimitMillis = 60000,
        memoryLimitBytes = 1610612736,
        numberOfProcesses = 1000,
        outputLimitBytes = 536870912,
        realTimeLimitMillis = 120000
    )
    val JAVA11 = SybonCompiler(
        id = 8,
        type = SybonCompiler.Type.java,
        name = "Java 11",
        description = "Java 11",
        args = "version=11",
        timeLimitMillis = 60000,
        memoryLimitBytes = 1610612736,
        numberOfProcesses = 1000,
        outputLimitBytes = 536870912,
        realTimeLimitMillis = 120000
    )
    val JAVA17 = SybonCompiler(
        id = 9,
        type = SybonCompiler.Type.java,
        name = "Java 17",
        description = "Java 17",
        args = "version=17",
        timeLimitMillis = 60000,
        memoryLimitBytes = 1610612736,
        numberOfProcesses = 1000,
        outputLimitBytes = 536870912,
        realTimeLimitMillis = 120000
    )
    val CPP17 = SybonCompiler(
        id = 10,
        type = SybonCompiler.Type.gcc,
        name = "C++17",
        description = "C++17",
        args = "lang=c++,optimize=2,std=c++17,fno-stack-limit",
        timeLimitMillis = 60000,
        memoryLimitBytes = 1610612736,
        numberOfProcesses = 1000,
        outputLimitBytes = 536870912,
        realTimeLimitMillis = 120000
    )
    val CPP20 = SybonCompiler(
        id = 11,
        type = SybonCompiler.Type.gcc,
        name = "C++20",
        description = "C++20",
        args = "lang=c++,optimize=2,std=c++20,fno-stack-limit",
        timeLimitMillis = 60000,
        memoryLimitBytes = 1610612736,
        numberOfProcesses = 1000,
        outputLimitBytes = 536870912,
        realTimeLimitMillis = 120000
    )
    val CSHARP_DOT_NET_6 = SybonCompiler(
        id = 12,
        type = SybonCompiler.Type.dotnet,
        name = "C# .NET 6.0",
        description = ".NET 6.0",
        args = "conf=Release,-no-restore,-use-current-runtime,-no-dependencies,-nologo,-disable-build-servers",
        timeLimitMillis = 60000,
        memoryLimitBytes = 1610612736,
        numberOfProcesses = 1000,
        outputLimitBytes = 536870912,
        realTimeLimitMillis = 120000
    )
    val KOTLIN = SybonCompiler(
        id = 13,
        type = SybonCompiler.Type.kotlin,
        name = "Kotlin",
        description = "Kotlin 1.7.21",
        args = "",
        timeLimitMillis = 60000,
        memoryLimitBytes = 1610612736,
        numberOfProcesses = 1000,
        outputLimitBytes = 536870912,
        realTimeLimitMillis = 120000
    )
    val GOLANG = SybonCompiler(
        id = 14,
        type = SybonCompiler.Type.golang,
        name = "Golang",
        description = "Golang 1.18.1",
        args = "",
        timeLimitMillis = 60000,
        memoryLimitBytes = 1610612736,
        numberOfProcesses = 1000,
        outputLimitBytes = 536870912,
        realTimeLimitMillis = 120000
    )
    val RUBY = SybonCompiler(
        id = 15,
        type = SybonCompiler.Type.ruby,
        name = "Ruby",
        description = "Ruby 3.0.2p107",
        args = "",
        timeLimitMillis = 60000,
        memoryLimitBytes = 1610612736,
        numberOfProcesses = 1000,
        outputLimitBytes = 536870912,
        realTimeLimitMillis = 120000
    )
    val JAVASCRIPT = SybonCompiler(
        id = 16,
        type = SybonCompiler.Type.node,
        name = "JavaScript",
        description = "JavaScript",
        args = "",
        timeLimitMillis = 60000,
        memoryLimitBytes = 1610612736,
        numberOfProcesses = 1000,
        outputLimitBytes = 536870912,
        realTimeLimitMillis = 120000
    )
}
