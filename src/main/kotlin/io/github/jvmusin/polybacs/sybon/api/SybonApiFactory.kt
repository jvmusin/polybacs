package io.github.jvmusin.polybacs.sybon.api

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient
import org.springframework.web.util.UriComponentsBuilder

@Component
class SybonApiFactory(
    @Value("\${sybon.apiKey}")
    private val apiKey: String,
) {
    private inline fun <reified T : Any> createApi(): T {
        val webClientBuilder = WebClient.builder()
            .filter { request, next ->
                val newUri = UriComponentsBuilder.fromUri(request.url()).queryParam("api_key", apiKey).build().toUri()
                val newRequest = ClientRequest.from(request).url(newUri).build()
                next.exchange(newRequest)
            }.codecs {
                it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) // 16 MB
            }
        return HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(webClientBuilder.build()))
            .build()
            .createClient<T>()
    }

    fun createArchiveApi(): SybonArchiveApi = createApi()
    fun createCheckingApi(): SybonCheckingApi = createApi()
}
