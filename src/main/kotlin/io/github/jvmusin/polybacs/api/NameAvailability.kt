package io.github.jvmusin.polybacs.api

enum class NameAvailability(val description: String) {
    AVAILABLE("Имя доступно"),
    TAKEN("Имя занято"),
    LOADING("Подгружаем"),
    CHECK_FAILED("Не удалось проверить доступность имени")
}
