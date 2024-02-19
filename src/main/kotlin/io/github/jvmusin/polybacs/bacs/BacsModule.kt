package io.github.jvmusin.polybacs.bacs

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BacsConfiguration {
    @Bean
    fun bacsConfig(
        @Value("\${bacs.host}") host: String,
        @Value("\${bacs.basePath}") basePath: String,
        @Value("\${bacs.username}") username: String,
        @Value("\${bacs.password}") password: String,
    ) = BacsConfig(host, basePath, username, password)

    @Bean
    fun bacsArchiveService(config: BacsConfig) = BacsArchiveServiceFactory(config).create()
}
