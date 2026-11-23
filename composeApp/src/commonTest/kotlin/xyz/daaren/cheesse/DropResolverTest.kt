package xyz.daaren.cheesse

import xyz.daaren.cheesse.domain.chess.ChessMove
import xyz.daaren.cheesse.domain.chess.ChessSquare
import xyz.daaren.cheesse.ui.game.board.DropDecision
import xyz.daaren.cheesse.ui.game.board.resolveDrop
import kotlin.test.Test
import kotlin.test.assertEquals

class DropResolverTest {
    private fun sq(name: String) = ChessSquare.fromString(name)

    private fun move(uci: String) = ChessMove(sq(uci.take(2)), sq(uci.substring(2, 4)), uci)

    private val legalMoves =
        listOf(move("e2e4"), move("e2e3"), move("a7a8q"), move("a7a8r"), move("a7a8b"), move("a7a8n"))

    @Test
    fun `dropping on a legal target plays that move`() {
        assertEquals(DropDecision.Play("e2e4"), resolveDrop(legalMoves, sq("e2"), sq("e4")))
    }

    @Test
    fun `dropping on a promotion square asks for the piece`() {
        assertEquals(DropDecision.Promote("a7a8"), resolveDrop(legalMoves, sq("a7"), sq("a8")))
    }

    @Test
    fun `dropping on an illegal target is rejected`() {
        assertEquals(DropDecision.Illegal, resolveDrop(legalMoves, sq("e2"), sq("e5")))
    }

    @Test
    fun `dropping back on the start square is rejected`() {
        assertEquals(DropDecision.Illegal, resolveDrop(legalMoves, sq("e2"), sq("e2")))
    }
}
