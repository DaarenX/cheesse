package xyz.daaren.cheesse

import io.github.alluhemanth.chess.core.ChessGame
import io.github.alluhemanth.chess.core.board.Square
import io.github.alluhemanth.chess.core.move.Move

fun ChessGame.makeUciMoveWorkaround(uci: String): Boolean {
    val fromSquare = Square(uci.substring(0, 2))
    val toSquare = Square(uci.substring(2, 4))
    val promotionChar = uci.getOrNull(4)
    val requestedMove = Move(fromSquare, toSquare, promotionChar)
    val legalMove =
        getLegalMoves().firstOrNull {
            it.from == requestedMove.from &&
                it.to == requestedMove.to &&
                it.promotionPieceType == requestedMove.promotionPieceType
        } ?: return false

    return makeMove(legalMove)
}
