package io.github.jvmusin.polybacs.polygon.converter

import io.github.jvmusin.polybacs.ir.IRLanguage

object PolygonSourceTypeToIRLanguageConverter {
    fun convert(sourceType: String): IRLanguage {
        return when (sourceType) {
            "cpp.g++17", "cpp,g++14", "cpp.g++11" -> IRLanguage.CPP
            "kotlin" -> IRLanguage.KOTLIN
            "python.2" -> IRLanguage.PYTHON2
            "python.3" -> IRLanguage.PYTHON3
            "java8", "java11" -> IRLanguage.JAVA
            else -> IRLanguage.OTHER
        }
    }
}
