package xyz.daaren.cheesse

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.api.ServerMessage
import xyz.daaren.cheesse.data.GameSessionConnection
import xyz.daaren.cheesse.data.MultiplayerGameSession
import xyz.daaren.cheesse.data.OfflineGameSession
import xyz.daaren.cheesse.domain.chess.ChessGameHistory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameSessionTest {
    private val fenAfterE4 = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"

    @Test
    fun `offline game session allows turn for both colors`() {
        val session = OfflineGameSession()
        assertTrue(session.isTurnAllowed(PlayerColor.WHITE))
        assertTrue(session.isTurnAllowed(PlayerColor.BLACK))
    }

    @Test
    fun `offline game session records every move in its history`() =
        runTest {
            val session = OfflineGameSession()
            session.sendMove("e2e4")
            session.sendMove("e7e5")
            assertEquals(ChessGameHistory(movesUci = listOf("e2e4", "e7e5")), session.updates.first())
        }

    @Test
    fun `multiplayer game session only allows turn for assigned player color`() {
        val session = multiplayerSession(emptyFlow())
        assertTrue(session.isTurnAllowed(PlayerColor.WHITE))
        assertFalse(session.isTurnAllowed(PlayerColor.BLACK))
    }

    @Test
    fun `multiplayer game session emits history sent by server`() =
        runTest {
            val message = ServerMessage.Move(fen = fenAfterE4, startFen = DEFAULT_FEN, movesUci = listOf("e2e4"))
            val session = multiplayerSession(flowOf(message))
            assertEquals(ChessGameHistory(DEFAULT_FEN, listOf("e2e4")), session.updates.first())
        }

    @Test
    fun `multiplayer game session falls back to FEN when server sends no history`() =
        runTest {
            val session = multiplayerSession(flowOf(ServerMessage.Move(fen = fenAfterE4)))
            assertEquals(ChessGameHistory(startFen = fenAfterE4), session.updates.first())
        }

    private fun multiplayerSession(updates: Flow<ServerMessage>) =
        MultiplayerGameSession(
            id = 1L,
            playerToken = "token-1",
            playerColor = PlayerColor.WHITE,
            gameSessionConnection = GameSessionConnection(updates) {},
        )
}
