package io.github.jvmusin.polybacs.polygon

import io.github.jvmusin.polybacs.polygon.api.PolygonApi
import io.github.jvmusin.polybacs.polygon.api.PolygonApiFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PolygonModule {
    @Bean
    fun polygonConfig(
        @Value("\${polygon.apiKey}") apiKey: String,
        @Value("\${polygon.secret}") secret: String,
    ) = PolygonConfig(apiKey, secret)

    @Bean
    fun polygonApi(polygonConfig: PolygonConfig) = PolygonApiFactory(polygonConfig).create()

    @Bean
    fun polygonProblemDownloader(polygonApi: PolygonApi) = PolygonProblemDownloaderImpl(polygonApi)

    @Bean
    fun polygonService(polygonApi: PolygonApi, polygonProblemDownloader: PolygonProblemDownloader) =
        PolygonService(polygonApi, polygonProblemDownloader)
}
