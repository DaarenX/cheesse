package xyz.daaren.cheesse.domain.chess

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChessBoardHighlightsTest {
    private val adapter = DefaultChessEngineAdapter()

    private fun boardAfter(vararg moves: String) = adapter.getBoardState(ChessGameHistory(movesUci = moves.toList()))

    @Test
    fun `board state has no last move before the first move`() {
        assertNull(boardAfter().lastMove)
    }

    @Test
    fun `board state exposes last move`() {
        val lastMove = boardAfter("e2e4", "e7e5").lastMove
        assertEquals(ChessSquare.fromString("e7"), lastMove?.from)
        assertEquals(ChessSquare.fromString("e5"), lastMove?.to)
    }

    @Test
    fun `board state reports no checked king without check`() {
        assertNull(boardAfter("e2e4", "e7e5").checkedKingSquare)
    }

    @Test
    fun `board state reports checked king square`() {
        val boardState = boardAfter("e2e4", "f7f6", "d1h5")
        assertEquals(ChessSquare.fromString("e8"), boardState.checkedKingSquare)
    }

    @Test
    fun `board state reports checked king square on checkmate`() {
        val boardState = boardAfter("f2f3", "e7e5", "g2g4", "d8h4")
        assertEquals(ChessSquare.fromString("e1"), boardState.checkedKingSquare)
    }
}
