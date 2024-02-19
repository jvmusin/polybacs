package io.github.jvmusin.polybacs

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@SpringBootApplication
class PolybacsApplication

fun main(args: Array<String>) {
    runApplication<PolybacsApplication>(*args)
}

@RestController
class HelloController {
    @GetMapping("/")
    fun hello(request: HttpServletRequest, response: HttpServletResponse) {
        // get cookie
        val cookies = request.cookies
        if (cookies == null || cookies.isEmpty()) println("NO COOKIES")
        else cookies.forEach { println("COOKIES: " + it.name + "=" + it.value) }

        // set cors to allow everything
        response.addHeader("Access-Control-Allow-Origin", "*")
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        response.addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization")
        response.addHeader("Access-Control-Max-Age", "3600")

        // Set cookie "poly" to current time, and make it SameSite=None; Secure
        response.addCookie(Cookie("poly", LocalDateTime.now().toString()).apply {
            isHttpOnly = true
            maxAge = 3600
            secure = true
            this.setAttribute("SameSite", "None")
            this.setAttribute("Partitioned", "true")
        })
    }
}
