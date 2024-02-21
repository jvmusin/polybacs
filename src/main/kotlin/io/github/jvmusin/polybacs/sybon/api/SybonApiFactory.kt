package io.github.jvmusin.polybacs.sybon.api

import io.github.jvmusin.polybacs.retrofit.RetrofitClientFactory
import okhttp3.Interceptor
import okhttp3.Response
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Sybon API factory.
 *
 * Used to create [SybonArchiveApi] and [SybonCheckingApi].
 *
 * It uses [RetrofitClientFactory] under the hood to make the actual requests.
 *
 * @constructor Creates Sybon API factory.
 * @property config Sybon configuration, used to configure proper *apiKey* and system urls.
 */
@Component
class SybonApiFactory(
    @Value("\${sybon.apiKey}")
    private val apiKey: String,
) {

    /**
     * ApiKey injector interceptor
     *
     * Injects *apiKey* to every request made to the API.
     *
     * @constructor Creates *apiKey* injector interceptor.
     */
    private inner class ApiKeyInjectorInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val newUrl = chain.request().url.newBuilder().addQueryParameter("api_key", apiKey).build()
            val newRequest = chain.request().newBuilder().url(newUrl).build()
            return chain.proceed(newRequest)
        }
    }

    private inline fun <reified T> createApi(url: String): T = RetrofitClientFactory.create(url) {
        addInterceptor(ApiKeyInjectorInterceptor())
    }

    fun createArchiveApi(): SybonArchiveApi = createApi("https://archive.sybon.org/api/")
    fun createCheckingApi(): SybonCheckingApi = createApi("https://checking.sybon.org/api/")
}
