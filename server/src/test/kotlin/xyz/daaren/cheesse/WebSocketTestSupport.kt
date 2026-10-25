package xyz.daaren.cheesse

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import xyz.daaren.cheesse.api.ClientMessage
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeUnit

data class WebSocketConnection(
    val webSocket: WebSocket,
    val firstMessage: CompletableFuture<String>,
)

fun movePayload(moveUci: String): String = Json.encodeToString<ClientMessage>(ClientMessage.Move(moveUci))

/** Connects, optionally sends [payload], and returns the first message (or `CLOSE:<code>:<reason>`). */
fun exchangeWebSocketMessage(
    port: Int,
    gamePath: String,
    payload: String,
): String {
    val connection = openWebSocket(port, gamePath, payload)

    return try {
        connection.firstMessage.get(5, TimeUnit.SECONDS)
    } finally {
        connection.webSocket.abort()
    }
}

/** [gamePath] is `<gameId>?playerToken=<token>`. */
fun openWebSocket(
    port: Int,
    gamePath: String,
    initialPayload: String? = null,
): WebSocketConnection {
    val firstMessage = CompletableFuture<String>()
    val webSocket =
        HttpClient
            .newHttpClient()
            .newWebSocketBuilder()
            .buildAsync(URI.create("ws://localhost:$port/ws/game-session/$gamePath"), FirstMessageListener(firstMessage, initialPayload))
            .get(5, TimeUnit.SECONDS)

    return WebSocketConnection(webSocket, firstMessage)
}

fun gamePath(
    gameId: Long,
    playerToken: String,
): String = "$gameId?playerToken=$playerToken"

private class FirstMessageListener(
    private val firstMessage: CompletableFuture<String>,
    private val initialPayload: String?,
) : WebSocket.Listener {
    private val buffer = StringBuilder()

    override fun onOpen(webSocket: WebSocket) {
        webSocket.request(1)
        initialPayload?.let { webSocket.sendText(it, true) }
    }

    override fun onText(
        webSocket: WebSocket,
        data: CharSequence,
        last: Boolean,
    ): CompletionStage<*> {
        buffer.append(data)
        if (last && !firstMessage.isDone) {
            firstMessage.complete(buffer.toString())
        }
        webSocket.request(1)
        return CompletableFuture.completedFuture(null)
    }

    override fun onClose(
        webSocket: WebSocket,
        statusCode: Int,
        reason: String,
    ): CompletionStage<*> {
        firstMessage.complete("CLOSE:$statusCode:$reason")
        return CompletableFuture.completedFuture(null)
    }

    override fun onError(
        webSocket: WebSocket,
        error: Throwable,
    ) {
        firstMessage.completeExceptionally(error)
    }
}
