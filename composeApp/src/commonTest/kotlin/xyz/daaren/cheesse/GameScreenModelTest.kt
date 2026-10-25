package xyz.daaren.cheesse

import kotlinx.coroutines.flow.emptyFlow
import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.data.GameSession
import xyz.daaren.cheesse.domain.chess.ChessGameHistory
import xyz.daaren.cheesse.domain.chess.ChessGameResult
import xyz.daaren.cheesse.domain.chess.DefaultChessEngineAdapter
import xyz.daaren.cheesse.ui.game.GameScreenModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameScreenModelTest {
    private class FakeGameSession(
        override val playerColor: PlayerColor = PlayerColor.WHITE,
        private val turnAllowed: (PlayerColor) -> Boolean = { true },
    ) : GameSession {
        override val updates = emptyFlow<ChessGameHistory>()
        val sentMoves = mutableListOf<String>()

        override fun isTurnAllowed(color: PlayerColor): Boolean = turnAllowed(color)

        override suspend fun sendMove(moveUci: String) {
            sentMoves.add(moveUci)
        }
    }

    @Test
    fun `initial game state reflects starting board and legal moves`() {
        val fakeSession = FakeGameSession(playerColor = PlayerColor.WHITE)
        val screenModel = GameScreenModel(fakeSession, DefaultChessEngineAdapter())

        val state = screenModel.gameState.value
        assertEquals(32, state.piecePositions.size)
        assertEquals(PlayerColor.WHITE, state.turn)
        assertEquals(20, state.legalMoves.size)
        assertTrue(state.capturedPieces.isEmpty())
        assertEquals(ChessGameResult.Ongoing, state.gameResult)
    }

    @Test
    fun `disallows legal moves when not player turn`() {
        val fakeSession = FakeGameSession(playerColor = PlayerColor.BLACK, turnAllowed = { false })
        val screenModel = GameScreenModel(fakeSession, DefaultChessEngineAdapter())

        val state = screenModel.gameState.value
        assertTrue(state.legalMoves.isEmpty())
        assertEquals(PlayerColor.WHITE, state.turn)
    }

    @Test
    fun `makeMove updates state optimistically`() {
        val fakeSession = FakeGameSession(playerColor = PlayerColor.WHITE)
        val screenModel = GameScreenModel(fakeSession, DefaultChessEngineAdapter())

        screenModel.makeMove("e2e4")

        val state = screenModel.gameState.value
        assertEquals(PlayerColor.BLACK, state.turn)
    }

    @Test
    fun `repeating a position three times ends the game in a draw`() {
        val screenModel = GameScreenModel(FakeGameSession(), DefaultChessEngineAdapter())

        repeat(2) { listOf("g1f3", "g8f6", "f3g1", "f6g8").forEach(screenModel::makeMove) }

        val state = screenModel.gameState.value
        assertEquals(ChessGameResult.Draw.ThreefoldRepetition, state.gameResult)
        assertTrue(state.legalMoves.isEmpty())
    }
}
