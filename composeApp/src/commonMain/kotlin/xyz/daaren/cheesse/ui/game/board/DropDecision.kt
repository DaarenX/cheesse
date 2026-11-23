package xyz.daaren.cheesse.ui.game.board

import xyz.daaren.cheesse.domain.chess.ChessMove
import xyz.daaren.cheesse.domain.chess.ChessSquare

/** What dropping a dragged piece on a square means. */
sealed interface DropDecision {
    data class Play(
        val uci: String,
    ) : DropDecision

    /** Several legal moves share the target, so the player still has to pick a promotion piece. */
    data class Promote(
        val baseUci: String,
    ) : DropDecision

    data object Illegal : DropDecision
}

fun resolveDrop(
    legalMoves: List<ChessMove>,
    from: ChessSquare,
    to: ChessSquare,
): DropDecision {
    val candidates = legalMoves.filter { it.from == from && it.to == to }
    return when {
        candidates.size > 1 -> DropDecision.Promote(from.name + to.name)
        candidates.size == 1 -> DropDecision.Play(candidates.single().uci)
        else -> DropDecision.Illegal
    }
}
