package io.github.jvmusin.polybacs

import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.coroutines.EmptyCoroutineContext

@Component
class OffloadScope {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun launch(exceptionHandler: CoroutineExceptionHandler? = null, block: suspend CoroutineScope.() -> Unit) {
        scope.launch(exceptionHandler ?: EmptyCoroutineContext, block = block)
    }

    @PreDestroy
    fun close() {
        scope.cancel()
        LoggerFactory.getLogger(javaClass).info("OffloadScope has been cancelled")
    }
}
