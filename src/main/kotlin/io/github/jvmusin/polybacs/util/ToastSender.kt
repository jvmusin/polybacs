package io.github.jvmusin.polybacs.util

import io.github.jvmusin.polybacs.api.ToastKind

interface ToastSender {
    fun send(content: String, kind: ToastKind = ToastKind.INFORMATION)
}

//class ToastSenderFactory(sessionId: String) {
//    fun createSender(problem): ToastSender
//}
//
//interface ToastSender {
//    fun send(content: String, severity: ToastSeverity = ToastSeverity.NEUTRAL)
//}
//
//enum class ToastSeverity {
//    NEUTRAL,
//    SUCCESS,
//    FAILURE
//}