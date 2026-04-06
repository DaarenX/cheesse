package xyz.daaren.cheesse

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.test.web.reactive.server.WebTestClient
import xyz.daaren.cheesse.api.ClientMessage
import xyz.daaren.cheesse.api.CreateGameRequest
import xyz.daaren.cheesse.api.CreateGameResponse
import xyz.daaren.cheesse.api.GameColorPreference
import xyz.daaren.cheesse.api.JoinGameRequest
import xyz.daaren.cheesse.api.JoinGameResponse
import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.api.ServerMessage
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GameAuthIntegrationTest {
    @LocalServerPort
    private var port: Int = 0
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `anonymous users can create a game and join exactly once`() {
        val createResponse = createGame()

        assertTrue(createResponse.joinToken.isNotBlank())
        assertTrue(createResponse.playerToken.isNotBlank())
        assertEquals(PlayerColor.WHITE, createResponse.color)

        val joinResponse = joinGame(createResponse.joinToken)

        assertEquals(createResponse.gameId, joinResponse.gameId)
        assertNotEquals(createResponse.playerId, joinResponse.playerId)
        assertTrue(joinResponse.playerToken.isNotBlank())
        assertEquals(PlayerColor.BLACK, joinResponse.color)

        webTestClient()
            .post()
            .uri("/games/join")
            .bodyValue(JoinGameRequest(createResponse.joinToken))
            .exchange()
            .expectStatus()
            .isEqualTo(409)
    }

    @Test
    fun `creator can choose black and joiner becomes white`() {
        val createResponse = createGame(GameColorPreference.BLACK)

        assertEquals(PlayerColor.BLACK, createResponse.color)

        val joinResponse = joinGame(createResponse.joinToken)

        assertEquals(PlayerColor.WHITE, joinResponse.color)
        assertNotEquals(createResponse.playerId, joinResponse.playerId)
    }

    @Test
    fun `creator can request random color`() {
        val createResponse = createGame(GameColorPreference.RANDOM)

        assertTrue(createResponse.color == PlayerColor.WHITE || createResponse.color == PlayerColor.BLACK)

        val joinResponse = joinGame(createResponse.joinToken)
        val expectedJoinerColor = if (createResponse.color == PlayerColor.WHITE) PlayerColor.BLACK else PlayerColor.WHITE
        assertEquals(expectedJoinerColor, joinResponse.color)
    }

    @Test
    fun `preflight request from web client origin is accepted`() {
        webTestClient()
            .options()
            .uri("/games")
            .header(HttpHeaders.ORIGIN, "http://localhost:3000")
            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.POST.name())
            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000")
            .expectHeader()
            .value(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS) { headerValue ->
                assertNotNull(headerValue)
                assertTrue(headerValue.contains(HttpMethod.POST.name()))
            }
    }

    @Test
    fun `websocket accepts authenticated players only and does not leak auth tokens`() {
        val createResponse = createGame()
        val joinResponse = joinGame(createResponse.joinToken)

        val unauthorizedClose =
            exchangeWebSocketMessage(
                gameId = createResponse.gameId,
                playerToken = "invalid-token",
                payload = movePayload("e2e4"),
            )
        assertTrue(unauthorizedClose.startsWith("CLOSE:1008"), unauthorizedClose)

        val wrongTurnMessage =
            exchangeWebSocketMessage(
                gameId = createResponse.gameId,
                playerToken = joinResponse.playerToken,
                payload = movePayload("e7e5"),
            )
        assertTrue(wrongTurnMessage.contains("It is not your turn"), wrongTurnMessage)

        val validMoveMessage =
            exchangeWebSocketMessage(
                gameId = createResponse.gameId,
                playerToken = createResponse.playerToken,
                payload = movePayload("e2e4"),
            )
        assertTrue(
            validMoveMessage.contains("\"fen\":\"rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq"),
            validMoveMessage,
        )
        assertFalse(validMoveMessage.contains("joinToken"))
        assertFalse(validMoveMessage.contains("playerToken"))
    }

    @Test
    fun `websocket broadcasts game start when both players are connected`() {
        val createResponse = createGame()
        val creatorSocket = openWebSocket(createResponse.gameId, createResponse.playerToken)
        val joinResponse = joinGame(createResponse.joinToken)
        val joinerSocket = openWebSocket(createResponse.gameId, joinResponse.playerToken)

        try {
            assertEquals(
                ServerMessage.GameStart,
                json.decodeFromString<ServerMessage>(creatorSocket.firstMessage.get(5, TimeUnit.SECONDS)),
            )
            assertEquals(
                ServerMessage.GameStart,
                json.decodeFromString<ServerMessage>(joinerSocket.firstMessage.get(5, TimeUnit.SECONDS)),
            )
        } finally {
            creatorSocket.webSocket.abort()
            joinerSocket.webSocket.abort()
        }
    }

    private fun createGame(color: GameColorPreference = GameColorPreference.WHITE): CreateGameResponse {
        val response =
            webTestClient()
                .post()
                .uri("/games")
                .bodyValue(CreateGameRequest(color))
                .exchange()
                .expectStatus()
                .isCreated
                .expectBody(CreateGameResponse::class.java)
                .returnResult()
                .responseBody

        return assertNotNull(response)
    }

    private fun joinGame(joinToken: String): JoinGameResponse =
        webTestClient()
            .post()
            .uri("/games/join")
            .bodyValue(JoinGameRequest(joinToken))
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(JoinGameResponse::class.java)
            .returnResult()
            .responseBody!!

    private fun exchangeWebSocketMessage(
        gameId: Long,
        playerToken: String,
        payload: String,
    ): String {
        val connection = openWebSocket(gameId, playerToken, payload)

        return try {
            connection.firstMessage.get(5, TimeUnit.SECONDS)
        } finally {
            connection.webSocket.abort()
        }
    }

    private fun openWebSocket(
        gameId: Long,
        playerToken: String,
        initialPayload: String? = null,
    ): WebSocketConnection {
        val firstMessage = CompletableFuture<String>()
        val webSocket =
            HttpClient
                .newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(
                    URI.create("ws://localhost:$port/ws/game-session/$gameId?playerToken=$playerToken"),
                    object : WebSocket.Listener {
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
                            if (!firstMessage.isDone) {
                                firstMessage.complete("CLOSE:$statusCode:$reason")
                            }
                            return CompletableFuture.completedFuture(null)
                        }

                        override fun onError(
                            webSocket: WebSocket,
                            error: Throwable,
                        ) {
                            if (!firstMessage.isDone) {
                                firstMessage.completeExceptionally(error)
                            }
                        }
                    },
                ).get(5, TimeUnit.SECONDS)

        return WebSocketConnection(webSocket, firstMessage)
    }

    private fun movePayload(moveUci: String): String = json.encodeToString<ClientMessage>(ClientMessage.Move(moveUci))

    private fun webTestClient(): WebTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

    private data class WebSocketConnection(
        val webSocket: WebSocket,
        val firstMessage: CompletableFuture<String>,
    )
}
