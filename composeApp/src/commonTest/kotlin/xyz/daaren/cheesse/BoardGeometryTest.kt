package xyz.daaren.cheesse

import androidx.compose.ui.geometry.Offset
import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.domain.chess.ChessMove
import xyz.daaren.cheesse.domain.chess.ChessPieceType
import xyz.daaren.cheesse.domain.chess.ChessSquare
import xyz.daaren.cheesse.ui.game.board.moveOrigins
import xyz.daaren.cheesse.ui.game.board.squareOffset
import kotlin.test.Test
import kotlin.test.assertEquals

class BoardGeometryTest {
    private fun sq(name: String) = ChessSquare.fromString(name)

    private fun move(uci: String) = ChessMove(sq(uci.take(2)), sq(uci.substring(2, 4)), uci)

    @Test
    fun `square offset places a1 bottom left for white`() {
        assertEquals(Offset(0f, 700f), squareOffset(sq("a1"), PlayerColor.WHITE, 100f))
    }

    @Test
    fun `square offset places a1 top right for black`() {
        assertEquals(Offset(700f, 0f), squareOffset(sq("a1"), PlayerColor.BLACK, 100f))
    }

    @Test
    fun `move origins map destination to origin`() {
        assertEquals(mapOf(sq("e4") to sq("e2")), moveOrigins(move("e2e4"), ChessPieceType.PAWN))
    }

    @Test
    fun `move origins include the rook when castling kingside`() {
        val origins = moveOrigins(move("e1g1"), ChessPieceType.KING)
        assertEquals(mapOf(sq("g1") to sq("e1"), sq("f1") to sq("h1")), origins)
    }

    @Test
    fun `move origins include the rook when castling queenside`() {
        val origins = moveOrigins(move("e8c8"), ChessPieceType.KING)
        assertEquals(mapOf(sq("c8") to sq("e8"), sq("d8") to sq("a8")), origins)
    }

    @Test
    fun `move origins ignore a normal king step`() {
        assertEquals(mapOf(sq("f1") to sq("e1")), moveOrigins(move("e1f1"), ChessPieceType.KING))
    }
}
