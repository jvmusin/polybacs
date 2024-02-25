package io.github.jvmusin.polybacs.util

import io.github.jvmusin.polybacs.api.ToastKind

interface ToastSender {
    fun send(content: String, kind: ToastKind = ToastKind.INFORMATION)
}
