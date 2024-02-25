package io.github.jvmusin.polybacs.server

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class IndexController {
    @RequestMapping(value = ["/", "/problems/**"])
    fun index(): String {
        return "forward:index.html"
    }
}
