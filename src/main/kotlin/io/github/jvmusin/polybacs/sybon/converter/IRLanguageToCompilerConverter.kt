package io.github.jvmusin.polybacs.sybon.converter

import io.github.jvmusin.polybacs.ir.IRLanguage
import io.github.jvmusin.polybacs.sybon.SybonCompilers
import io.github.jvmusin.polybacs.sybon.api.SybonCompiler

object IRLanguageToCompilerConverter {
    private fun convert(language: IRLanguage): SybonCompiler? {
        return when (language) {
            IRLanguage.CPP -> SybonCompilers.CPP
            IRLanguage.JAVA -> SybonCompilers.JAVA17
            IRLanguage.PYTHON2 -> SybonCompilers.PYTHON2
            IRLanguage.PYTHON3 -> SybonCompilers.PYTHON3
            else -> null
        }
    }

    fun IRLanguage.toSybonCompiler() = convert(this)
}
