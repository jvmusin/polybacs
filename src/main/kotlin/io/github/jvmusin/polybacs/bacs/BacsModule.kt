package io.github.jvmusin.polybacs.bacs

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BacsConfiguration {
    @Bean
    fun bacsConfig(
        @Value("\${bacs.username}") username: String,
        @Value("\${bacs.password}") password: String,
    ) = BacsConfig(username, password)

    @Bean
    fun bacsArchiveService(config: BacsConfig) = BacsArchiveServiceFactory(config).create()
}
