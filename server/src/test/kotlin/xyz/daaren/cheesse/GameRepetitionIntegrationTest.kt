package xyz.daaren.cheesse

import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import xyz.daaren.cheesse.api.GameColorPreference
import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.domain.chess.ChessGameResult
import xyz.daaren.cheesse.domain.game.CreatedGame
import xyz.daaren.cheesse.domain.game.GameService
import xyz.daaren.cheesse.domain.game.JoinedGame
import xyz.daaren.cheesse.domain.game.MoveExecutionResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class GameRepetitionIntegrationTest {
    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:17-alpine")

        // The start position occurs for the third time after the last move.
        private val knightShuffle = listOf("g1f3", "g8f6", "f3g1", "f6g8", "g1f3", "g8f6", "f3g1", "f6g8")
    }

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var gameService: GameService

    @Test
    fun `move history is persisted across moves`() =
        runBlocking {
            val (created, joined) = startGame()
            playMoves(created, joined, listOf("e2e4", "e7e5"))

            val game = gameService.getGame(created.gameId)
            assertEquals(listOf("e2e4", "e7e5"), game.history.movesUci)
            assertEquals("e4 e5", game.pgn)
        }

    @Test
    fun `server rejects moves after a threefold repetition`() =
        runBlocking {
            val (created, joined) = startGame()
            playMoves(created, joined, knightShuffle)

            val result = gameService.executeMove(created.gameId, created.playerId, PlayerColor.WHITE, "e2e4")
            assertEquals(MoveExecutionResult.GameOver(ChessGameResult.Draw.ThreefoldRepetition), result)
        }

    @Test
    fun `websocket replies with an error to moves after the game is over`() {
        val (created, joined) = runBlocking { startGame().also { playMoves(it.first, it.second, knightShuffle) } }

        val reply = exchangeWebSocketMessage(port, gamePath(created.gameId, created.playerToken.toString()), movePayload("e2e4"))

        assertTrue(reply.contains("Game is over"), reply)
        assertEquals(knightShuffle, runBlocking { gameService.getGame(joined.gameId).history.movesUci })
    }

    private suspend fun startGame(): Pair<CreatedGame, JoinedGame> {
        val created = gameService.createGame(GameColorPreference.WHITE)
        return created to gameService.joinGame(created.joinToken.toString())
    }

    private suspend fun playMoves(
        white: CreatedGame,
        black: JoinedGame,
        moves: List<String>,
    ) {
        moves.forEachIndexed { index, moveUci ->
            val (playerId, color) =
                if (index % 2 == 0) white.playerId to PlayerColor.WHITE else black.playerId to PlayerColor.BLACK
            assertIs<MoveExecutionResult.Success>(gameService.executeMove(white.gameId, playerId, color, moveUci))
        }
    }
}
