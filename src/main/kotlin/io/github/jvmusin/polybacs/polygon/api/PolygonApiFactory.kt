package io.github.jvmusin.polybacs.polygon.api

import io.github.jvmusin.polybacs.polygon.PolygonConfig
import io.github.jvmusin.polybacs.util.sha512
import kotlinx.coroutines.delay
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.web.reactive.function.BodyExtractors
import org.springframework.web.reactive.function.client.*
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import java.net.URLDecoder
import kotlin.time.Duration.Companion.seconds

@Configuration
class PolygonApiFactory(
    private val config: PolygonConfig,
) {
    private val logger = LoggerFactory.getLogger(PolygonApiFactory::class.java)

    /**
     * Changes response code from `400` to `200`.
     *
     * Used to treat code `400` responses as code `200` responses.
     *
     * Polygon API returns code `400` when something is wrong,
     * but it also returns the message about that in request body,
     * so we will have `null` result and `non-null` message
     * in the [PolygonResponse].
     */
    private val responseCode400to200Filter = createFilter { request, next ->
        val response = next.exchange(request)
        when (response.statusCode()) {
            HttpStatus.BAD_REQUEST -> {
                val res = response.mutate()
                val body = response.body(BodyExtractors.toDataBuffers()).awaitSingle().toString(Charsets.UTF_8)
                if ("Too many requests. Please, wait few seconds and try again" in body) {
                    logger.info("Too many requests occurred, making code to 500 to repeat")
                    res.statusCode(HttpStatusCode.valueOf(500)).body(body).build()
                } else {
                    res.statusCode(HttpStatus.OK).build()
                }
            }

            else -> response
        }
    }

    /**
     * Changes response content type to *application/json*.
     *
     * Polygon API returns `text/html` content type, but it's actually `application/json`.
     */
    private val fixResponseContentTypeFilter = createFilter { request, next ->
        next.exchange(request).mutate()
            .headers { headers -> headers.contentType = MediaType.APPLICATION_JSON }.build()
    }

    val logRequestFilter = createFilter { request, next ->
        if (false) {
            val url = request.url()
            val exchange = next.exchange(request)
            try {
                val body1 = exchange.body(BodyExtractors.toDataBuffers())
                val b = body1.collectList().awaitSingle()
                println(b)
                println(url)
                return@createFilter exchange.mutate().body(Flux.empty()).build()
            } catch (e: Exception) {
                println(e.message)
                e.printStackTrace()
                throw e
            }
        }
        next.exchange(request)
    }

    private val insertApiSigFilter = createFilter { request, next ->
        val time = System.currentTimeMillis() / 1000
        val prefix = "abcdef"
        val method = request.url().path.removePrefix("/api/")

        val decodedUrl = URLDecoder.decode(request.url().toString(), Charsets.UTF_8)
        val builder = UriComponentsBuilder.fromUriString(decodedUrl)
            .queryParam("apiKey", config.apiKey)
            .queryParam("time", time.toString())
            .cloneBuilder()

        val middle = builder.build().queryParams
            .map { it.key to it.value.single() }
            .sortedWith(compareBy({ it.first }, { it.second }))
            .joinToString("&") { "${it.first}=${it.second}" }
        val toHash = "$prefix/$method?$middle#${config.secret}"
        val apiSig = prefix + toHash.sha512()

        val finalUrl = builder.queryParam("apiSig", apiSig).build().toUri()
        val newRequest = ClientRequest.from(request).url(finalUrl).build()
        next.exchange(newRequest)
    }

    private val maxInMemorySizeCodecConfigurer = { codecs: ClientCodecConfigurer ->
        codecs.defaultCodecs().maxInMemorySize(-1)
    }

    private val retryOn5xx = createFilter { request, next ->
        val totalRetries = 20 // Will run at most totalRetries+1 times
        val retryDelay = 2.seconds
        suspend fun go(retriesLeft: Int): ClientResponse {
            val response = next.exchange(request)
            val statusCode = response.statusCode()
            if (retriesLeft > 0 && response.statusCode().is5xxServerError) {
                logger.info("Got response code $statusCode from ${request.url()}, retrying in $retryDelay (retries left ${retriesLeft - 1})")
                delay(retryDelay)
                return go(retriesLeft - 1)
            }
            return response
        }
        go(totalRetries)
    }

    private inline fun <reified T : Any> createApi(): T {
        val webClientBuilder = WebClient.builder()
            .codecs(maxInMemorySizeCodecConfigurer)
            .filter(fixResponseContentTypeFilter)
            .filter(responseCode400to200Filter)
            .filter(retryOn5xx)
            .filter(insertApiSigFilter)
            .filter(logRequestFilter)
        // TODO: Add 500 and 429 retry filters
        //  429 is actually 400 with text in the body
        //  TOO_MANY_REQUESTS_MESSAGE = "Too many requests. Please, wait few seconds and try again"
        return HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(webClientBuilder.build()))
            .build()
            .createClient<T>()
    }

    @Bean
    fun polygonApi(): PolygonApi {
        return createApi()
    }

    private companion object {
        fun createFilter(filter: suspend (request: ClientRequest, next: CoExchangeFunction) -> ClientResponse): CoExchangeFilterFunction {
            return object : CoExchangeFilterFunction() {
                override suspend fun filter(request: ClientRequest, next: CoExchangeFunction): ClientResponse {
                    return filter(request, next)
                }
            }
        }
    }
}
