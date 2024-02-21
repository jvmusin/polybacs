package io.github.jvmusin.polybacs.sybon

import io.github.jvmusin.polybacs.sybon.api.SybonApiFactory
import io.github.jvmusin.polybacs.sybon.api.SybonArchiveApi
import io.github.jvmusin.polybacs.sybon.api.SybonCheckingApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SybonConfiguration {
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