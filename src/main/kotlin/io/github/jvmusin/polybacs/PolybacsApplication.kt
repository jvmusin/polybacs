package io.github.jvmusin.polybacs

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.jvmusin.polybacs.api.ToastKind
import io.github.jvmusin.polybacs.server.StatusTrackUpdate
import io.github.jvmusin.polybacs.util.ToastSender
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.handler.TextWebSocketHandler
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor

@SpringBootApplication
class PolybacsApplication

fun main(args: Array<String>) {
    runApplication<PolybacsApplication>(*args)
}

@Component
class WebSocketConnectionKeeper(
    private val offloadScope: OffloadScope,
) : TextWebSocketHandler() {
    private val handlers = hashMapOf<String, MutableList<WebSocketSession>>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        handlers.getOrPut(session.sessionId) { mutableListOf() }.add(session)
        println("Connection established with session: ${session.sessionId} with id: ${session.id}, active sessions: ${handlers.values.sumOf { it.size }}")
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        handlers[session.sessionId]?.remove(session)
        println("Connection closed with session: ${session.sessionId} with id: ${session.id}, active sessions: ${handlers.values.sumOf { it.size }}, reason: $status")
    }

    private val WebSocketSession.sessionId: String
        get() = this.attributes[HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME] as? String
            ?: throw IllegalStateException("No session id found in WebSocketSession attributes")

    public override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        println("Received message: " + message.payload + " from session: ${session.sessionId} with id: ${session.id}")
    }

    fun sendMessage(sessionId: String, message: Any) {
        val list = handlers[sessionId] ?: return
        val msg = jacksonObjectMapper().writer().writeValueAsString(message)
        for (session in list.filter { it.isOpen }) {
            offloadScope.launch {
                session.sendMessage(TextMessage(msg))
            }
        }
    }

    fun createSender(sessionId: String): ToastSender = object : ToastSender {
        override fun send(content: String, kind: ToastKind) {
            val list = handlers[sessionId] ?: return
            for (session in list.filter { it.isOpen }) {
                offloadScope.launch {
                    session.sendMessage(TextMessage("{\"content\":\"$content\",\"kind\":\"$kind\"}"))
                }
            }
        }
    }
}

interface StatusTrackUpdateSender {
    fun sendUpdate(update: TrackUpdate)

    sealed interface TrackUpdate
    data class TrackCreated(val trackId: Int, val problemId: Int, val problemName: String) : TrackUpdate
    data class TrackUpdated(val trackId: Int, val update: StatusTrackUpdate) : TrackUpdate
}

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val connectionKeeper: WebSocketConnectionKeeper,
) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry
            .addHandler(connectionKeeper, "/ws")
            .setAllowedOrigins("*")
            .addInterceptors(HttpSessionHandshakeInterceptor().apply {
                isCreateSession = true
            })
    }
}
