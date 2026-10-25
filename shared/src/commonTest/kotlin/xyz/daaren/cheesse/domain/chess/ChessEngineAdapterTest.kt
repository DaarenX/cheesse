package xyz.daaren.cheesse.domain.chess

import xyz.daaren.cheesse.api.PlayerColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ChessEngineAdapterTest {
    private val adapter = DefaultChessEngineAdapter()
    private val startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

    @Test
    fun `detects active player color from FEN`() {
        assertEquals(PlayerColor.WHITE, adapter.activePlayerColor(startFen))
        assertEquals(
            PlayerColor.BLACK,
            adapter.activePlayerColor("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1"),
        )
    }

    @Test
    fun `applies valid move and updates FEN`() {
        val result = adapter.applyMove(ChessGameHistory(), "e2e4")
        assertIs<ChessMoveResult.Success>(result)
        assertEquals(PlayerColor.BLACK, adapter.activePlayerColor(result.fen))
    }

    @Test
    fun `applied move is appended to history`() {
        val history = ChessGameHistory(movesUci = listOf("e2e4"))
        val result = adapter.applyMove(history, "e7e5")
        assertIs<ChessMoveResult.Success>(result)
        assertEquals(ChessGameHistory(startFen, listOf("e2e4", "e7e5")), result.history)
    }

    @Test
    fun `pgn contains every move of the game`() {
        val result = adapter.applyMove(ChessGameHistory(movesUci = listOf("e2e4", "e7e5")), "g1f3")
        assertIs<ChessMoveResult.Success>(result)
        assertEquals("e4 e5 Nf3", result.pgn)
    }

    @Test
    fun `rejects illegal move`() {
        val result = adapter.applyMove(ChessGameHistory(), "e2e5")
        assertIs<ChessMoveResult.IllegalMove>(result)
        assertEquals("e2e5", result.moveUci)
    }

    @Test
    fun `rejects malformed move notation`() {
        listOf("", "e2", "z9e4", "e2e4e5x").forEach { moveUci ->
            assertIs<ChessMoveResult.IllegalMove>(adapter.applyMove(ChessGameHistory(), moveUci), moveUci)
        }
    }

    @Test
    fun `board state replays history from start FEN`() {
        val boardState = adapter.getBoardState(ChessGameHistory(movesUci = listOf("e2e4", "e7e5")))
        assertEquals(PlayerColor.WHITE, boardState.turn)
        assertTrue(boardState.piecePositions.any { it.second == ChessSquare.fromString("e4") })
    }

    @Test
    fun `retrieves initial board state with correct pieces and legal moves`() {
        val boardState = adapter.getBoardState(ChessGameHistory())
        assertEquals(32, boardState.piecePositions.size)
        assertEquals(PlayerColor.WHITE, boardState.turn)
        assertEquals(20, boardState.legalMoves.size)
        assertTrue(boardState.capturedPieces.isEmpty())
        assertEquals(ChessGameResult.Ongoing, boardState.gameResult)
    }

    @Test
    fun `detects checkmate result`() {
        val foolsMate = ChessGameHistory(movesUci = listOf("f2f3", "e7e5", "g2g4", "d8h4"))
        val boardState = adapter.getBoardState(foolsMate)
        assertIs<ChessGameResult.Win>(boardState.gameResult)
        assertEquals(PlayerColor.BLACK, boardState.gameResult.winner)
    }
}
