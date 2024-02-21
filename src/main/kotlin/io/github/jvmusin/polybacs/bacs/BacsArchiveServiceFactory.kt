package io.github.jvmusin.polybacs.bacs

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import io.ktor.client.features.logging.*
import io.ktor.http.URLProtocol.Companion.HTTPS
import kotlin.time.Duration.Companion.minutes

/** Factory for [BacsArchiveService]. */
class BacsArchiveServiceFactory(private val config: BacsConfig) {

    /** Creates [BacsArchiveService] using auth credentials and other settings from [config]. */
    fun create(): BacsArchiveService {
        val client = HttpClient(CIO) {
            install(Auth) {
                basic {
                    sendWithoutRequest { true }
                    credentials { BasicAuthCredentials(config.username, config.password) }
                }
            }
            install(Logging) { level = LogLevel.INFO }
            defaultRequest {
                url {
                    protocol = HTTPS
                    host = "archive.bacs.cs.istu.ru"
                    encodedPath = "repository/$encodedPath"
                }
            }
            engine {
                this.requestTimeout = 10.minutes.inWholeMilliseconds
            }
        }
        return BacsArchiveService(client)
    }
}
