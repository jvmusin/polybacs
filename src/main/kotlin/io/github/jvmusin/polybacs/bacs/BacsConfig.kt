package io.github.jvmusin.polybacs.bacs

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
data class BacsConfig(
    @Value("\${bacs.username}")
    val username: String,
    @Value("\${bacs.password}")
    val password: String,
)
