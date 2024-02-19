package io.github.jvmusin.polybacs.api

data class Toast(
    val title: String,
    val content: String,
    val kind: ToastKind = ToastKind.INFORMATION
)
