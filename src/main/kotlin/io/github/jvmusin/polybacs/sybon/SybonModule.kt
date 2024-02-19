package io.github.jvmusin.polybacs.sybon

import io.github.jvmusin.polybacs.sybon.api.SybonApiFactory
import io.github.jvmusin.polybacs.sybon.api.SybonArchiveApi
import io.github.jvmusin.polybacs.sybon.api.SybonCheckingApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SybonConfiguration {
    @Bean
    fun sybonApiFactory(
        @Value("\${sybon.archiveApiUrl}") archiveApiUrl: String,
        @Value("\${sybon.checkingApiUrl}") checkingApiUrl: String,
        @Value("\${sybon.apiKey}") apiKey: String,
    ) = SybonApiFactory(SybonConfig(archiveApiUrl, checkingApiUrl, apiKey))

    @Bean
    fun sybonArchiveApi(sybonApiFactory: SybonApiFactory) = sybonApiFactory.createArchiveApi()

    @Bean
    fun sybonCheckingApi(sybonApiFactory: SybonApiFactory) = sybonApiFactory.createCheckingApi()

    @Bean
    fun testSybonArchiveService(sybonArchiveApi: SybonArchiveApi) =
        SybonArchiveService(sybonArchiveApi, TestProblemArchive.collectionId)

    @Bean
    fun mainSybonArchiveService(sybonArchiveApi: SybonArchiveApi) =
        SybonArchiveService(sybonArchiveApi, MainProblemArchive.collectionId)

    @Bean
    fun sybonCheckingService(sybonCheckingApi: SybonCheckingApi) = SybonCheckingService(sybonCheckingApi)
}