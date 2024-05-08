package io.github.jvmusin.polybacs.server

import io.github.jvmusin.polybacs.WebSocketConnectionKeeper
import org.springframework.stereotype.Component

@Component
class StatusTracker(private val webSocketConnectionKeeper: WebSocketConnectionKeeper) {
    private val tracks = mutableListOf<StatusTrack>()

    fun newTrack(problemId: Int, problemName: String, sessionId: String): StatusTrackUpdateConsumer {
        val track = StatusTrack(tracks.size, problemId, problemName)
        tracks.add(track)
        return StatusTrackUpdateConsumer { message, severity ->
            webSocketConnectionKeeper.sendMessage(
                sessionId,
                StatusUpdateMessage(
                    track.copy(),
                    message,
                    severity
                )
            )
        }
    }

    data class StatusUpdateMessage(
        val track: StatusTrack,
        val message: String,
        val severity: StatusTrackUpdateSeverity
    )
}

data class StatusTrack(
    val id: Int,
    val problemId: Int,
    val problemName: String,
) {
    private val updates = mutableListOf<StatusTrackUpdate>()

    val isFinal: Boolean
        get() = updates.lastOrNull()?.severity?.isFinal ?: false

    fun addUpdate(message: String, severity: StatusTrackUpdateSeverity): StatusTrackUpdate {
        val update = StatusTrackUpdate(updates.size, message, severity)
        updates.add(update)
        return update
    }

    fun getUpdates(): List<StatusTrackUpdate> = updates.toList()
}

data class StatusTrackUpdate(
    val id: Int,
    val message: String,
    val severity: StatusTrackUpdateSeverity,
)

enum class StatusTrackUpdateSeverity(val isFinal: Boolean) {
    NEUTRAL(isFinal = false),
    SUCCESS(isFinal = true),
    FAILURE(isFinal = true)
}

fun interface StatusTrackUpdateConsumer {
    fun consumeUpdate(
        message: String,
        severity: StatusTrackUpdateSeverity
    )
}
