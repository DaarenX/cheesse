package xyz.daaren.cheesse

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import xyz.daaren.cheesse.api.game.dto.CreateGameRequest
import xyz.daaren.cheesse.api.game.dto.CreateGameResponse
import xyz.daaren.cheesse.api.game.dto.JoinGameRequest
import xyz.daaren.cheesse.api.game.dto.JoinGameResponse
import xyz.daaren.cheesse.domain.game.GameColorPreference
import xyz.daaren.cheesse.domain.game.PlayerColor
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
    fun `websocket accepts authenticated players only and does not leak auth tokens`() {
        val createResponse = createGame()
        val joinResponse = joinGame(createResponse.joinToken)

        val unauthorizedClose =
            exchangeWebSocketMessage(
                gameId = createResponse.gameId,
                playerToken = "invalid-token",
                payload = """{"moveUci":"e2e4"}""",
            )
        assertTrue(unauthorizedClose.startsWith("CLOSE:1008"), unauthorizedClose)

        val wrongTurnMessage =
            exchangeWebSocketMessage(
                gameId = createResponse.gameId,
                playerToken = joinResponse.playerToken,
                payload = """{"moveUci":"e7e5"}""",
            )
        assertTrue(wrongTurnMessage.contains("It is not your turn"), wrongTurnMessage)

        val validMoveMessage =
            exchangeWebSocketMessage(
                gameId = createResponse.gameId,
                playerToken = createResponse.playerToken,
                payload = """{"moveUci":"e2e4"}""",
            )
        assertTrue(
            validMoveMessage.contains("\"fen\":\"rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq"),
            validMoveMessage,
        )
        assertFalse(validMoveMessage.contains("joinToken"))
        assertFalse(validMoveMessage.contains("playerToken"))
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
                            webSocket.sendText(payload, true)
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

        return try {
            firstMessage.get(5, TimeUnit.SECONDS)
        } finally {
            webSocket.abort()
        }
    }

    private fun webTestClient(): WebTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
}
