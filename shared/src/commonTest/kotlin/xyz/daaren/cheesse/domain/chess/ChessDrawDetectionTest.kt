package xyz.daaren.cheesse.domain.chess

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ChessDrawDetectionTest {
    private val adapter = DefaultChessEngineAdapter()

    // Knights hop out and back twice, so the start position occurs for the third time after 8 half moves.
    private val knightShuffle = listOf("g1f3", "g8f6", "f3g1", "f6g8", "g1f3", "g8f6", "f3g1", "f6g8")

    @Test
    fun `detects stalemate when side to move has no legal moves and is not in check`() {
        val boardState = adapter.getBoardState(ChessGameHistory("7k/5Q2/6K1/8/8/8/8/8 b - - 0 1"))
        assertEquals(ChessGameResult.Draw.Stalemate, boardState.gameResult)
        assertTrue(boardState.legalMoves.isEmpty())
    }

    @Test
    fun `detects fifty move rule draw at one hundred half moves`() {
        assertResult(ChessGameResult.Draw.FiftyMoveRule, ChessGameHistory("8/8/4k3/8/8/4K3/4R3/8 w - - 100 80"))
    }

    @Test
    fun `game is ongoing at ninety nine half moves`() {
        assertResult(ChessGameResult.Ongoing, ChessGameHistory("8/8/4k3/8/8/4K3/4R3/8 w - - 99 80"))
    }

    @Test
    fun `quiet move reaching one hundred half moves ends game by fifty move rule`() {
        val result = adapter.applyMove(ChessGameHistory("8/8/4k3/8/8/4K3/4R3/8 w - - 99 80"), "e2a2")
        assertIs<ChessMoveResult.Success>(result)
        assertResult(ChessGameResult.Draw.FiftyMoveRule, result.history)
    }

    @Test
    fun `detects insufficient material for lone kings and single minor pieces`() {
        listOf(
            "8/8/4k3/8/8/4K3/8/8 w - - 0 1",
            "8/8/4k3/8/8/4K3/4B3/8 w - - 0 1",
            "8/8/4k3/8/8/4K3/4N3/8 b - - 0 1",
        ).forEach { fen -> assertResult(ChessGameResult.Draw.InsufficientMaterial, ChessGameHistory(fen)) }
    }

    @Test
    fun `bishops on same colored squares are insufficient but opposite colors are not`() {
        assertResult(ChessGameResult.Draw.InsufficientMaterial, ChessGameHistory("5b1k/8/8/8/8/8/8/2B1K3 w - - 0 1"))
        assertResult(ChessGameResult.Ongoing, ChessGameHistory("2b4k/8/8/8/8/8/8/2B1K3 w - - 0 1"))
    }

    @Test
    fun `capturing last piece ends game by insufficient material`() {
        val result = adapter.applyMove(ChessGameHistory("8/8/8/8/2k1r3/4K3/8/8 w - - 0 1"), "e3e4")
        assertIs<ChessMoveResult.Success>(result)
        assertResult(ChessGameResult.Draw.InsufficientMaterial, result.history)
    }

    @Test
    fun `position occurring twice is not yet a threefold repetition`() {
        assertResult(ChessGameResult.Ongoing, ChessGameHistory(movesUci = knightShuffle.dropLast(1)))
    }

    @Test
    fun `move completing third occurrence of a position ends game by threefold repetition`() {
        val result = adapter.applyMove(ChessGameHistory(movesUci = knightShuffle.dropLast(1)), knightShuffle.last())
        assertIs<ChessMoveResult.Success>(result)
        val boardState = adapter.getBoardState(result.history)
        assertEquals(ChessGameResult.Draw.ThreefoldRepetition, boardState.gameResult)
        assertTrue(boardState.legalMoves.isEmpty())
    }

    @Test
    fun `rejects moves once the game is drawn`() {
        val result = adapter.applyMove(ChessGameHistory(movesUci = knightShuffle), "e2e4")
        assertIs<ChessMoveResult.GameOver>(result)
        assertEquals(ChessGameResult.Draw.ThreefoldRepetition, result.gameResult)
    }

    private fun assertResult(
        expected: ChessGameResult,
        history: ChessGameHistory,
    ) {
        assertEquals(expected, adapter.getBoardState(history).gameResult)
    }
}
