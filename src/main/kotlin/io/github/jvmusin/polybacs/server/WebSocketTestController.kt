package io.github.jvmusin.polybacs.server

import io.github.jvmusin.polybacs.WebSocketConnectionKeeper
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
class WebSocketTestController(
    private val webSocketConnectionKeeper: WebSocketConnectionKeeper,
) {
    @GetMapping("/test")
    fun test(session: HttpSession, response: HttpServletResponse) {
        webSocketConnectionKeeper.createSender(session.id).send("Hey you! This is a pong!")
    }
}
